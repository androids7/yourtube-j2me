package com.derevenetz.oleg.yourtube;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebSettings;

import com.derevenetz.oleg.yourtube.CustomDialogFragment;
import com.derevenetz.oleg.yourtube.CustomDialogFragment.CustomDialogFragmentListener;
import com.derevenetz.oleg.yourtube.MetadataDownloader;
import com.derevenetz.oleg.yourtube.MetadataDownloader.MetadataDownloaderListener;
import com.derevenetz.oleg.yourtube.VideoDownloader;
import com.derevenetz.oleg.yourtube.VideoDownloader.VideoDownloaderListener;

public class YourTubeActivity extends Activity implements MetadataDownloaderListener, VideoDownloaderListener, CustomDialogFragmentListener {
	private MetadataDownloader metadataDownloader = null;
	private VideoDownloader    videoDownloader    = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_yourtube);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.your_tube, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_download) {
        	WebView web_view = (WebView)findViewById(R.id.webview);

        	if (web_view != null) {
        		String url = web_view.getUrl();
        		
        		if (url.contains("/watch?")) {
        			String video_id = "";
        			
        			Pattern pat_1 = Pattern.compile("&v=([^&]+)");
        			Matcher mat_1 = pat_1.matcher(url);
        			
        			while (mat_1.find()) {
        				video_id = mat_1.group(1);
        				
        				break;
        			}

        			if (video_id.isEmpty()) {
            			Pattern pat_2 = Pattern.compile("\\?v=([^&]+)");
            			Matcher mat_2 = pat_2.matcher(url);
            			
            			while (mat_2.find()) {
            				video_id = mat_2.group(1);
            				
            				break;
            			}
        			}
        			
        			if (!video_id.isEmpty()) {
        				Uri.Builder builder = new Uri.Builder();

        				builder.scheme("http").authority("www.youtube.com").appendPath("get_video_info").appendQueryParameter("video_id", video_id)
        				                                                                                .appendQueryParameter("el", "detailpage")
        				                                                                                .appendQueryParameter("ps", "default")
        				                                                                                .appendQueryParameter("eurl", "")
        				                                                                                .appendQueryParameter("gl", "US")
        				                                                                                .appendQueryParameter("hl", "en");
        				
        				ShowMetadataProgressDialog();

        				if (metadataDownloader == null) {
            				metadataDownloader = new MetadataDownloader(this); 
            				metadataDownloader.execute(builder.build().toString());
        				}
        			}
        		}
        	}
        	
            return true;
        } else if (id == R.id.action_refresh) {
        	WebView web_view = (WebView)findViewById(R.id.webview);

        	if (web_view != null) {
        		web_view.reload();
        	}
        	
            return true;
        } else if (id == R.id.action_home) {
        	WebView web_view = (WebView)findViewById(R.id.webview);

        	if (web_view != null) {
        		web_view.loadUrl("http://m.youtube.com/");
        	}
        	
            return true;
        } else if (id == R.id.action_help) {
        	WebView web_view = (WebView)findViewById(R.id.webview);

        	if (web_view != null) {
        		web_view.loadUrl(getString(R.string.uri_help));
        	}
        	
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	WebView web_view = (WebView)findViewById(R.id.webview);
    	
    	if ((keyCode == KeyEvent.KEYCODE_BACK) && web_view != null && web_view.canGoBack() &&
    			                                !(web_view.getUrl().equals("http://m.youtube.com/") && web_view.copyBackForwardList().getCurrentIndex() <= 1)) {
    		web_view.goBack();
    		
    		return true;
    	} else {
    		return super.onKeyDown(keyCode, event);
    	}
    }

    @Override
    public void onMetadataProgressCancelled() {
		if (metadataDownloader != null) {
    		metadataDownloader.cancel(true);
		}
    }
    
    @Override
    public void onVideoProgressCancelled() {
		if (videoDownloader != null) {
    		videoDownloader.cancel(true);
		}
    }
    
    @Override
    public void onFormatSelected(String video_title, String itag, String extension, String url) {
    	SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
    	
    	editor.putString("PreferredITag", itag);
    	editor.commit();
    	
    	String file_name = video_title;
    	
    	file_name = file_name.replace('\\', '_');
    	file_name = file_name.replace('/',  '_');
    	file_name = file_name.replace(':',  '_');
    	file_name = file_name.replace(';',  '_');
    	file_name = file_name.replace('*',  '_');
    	file_name = file_name.replace('+',  '_');
    	file_name = file_name.replace('?',  '_');
    	file_name = file_name.replace('"',  '_');
    	file_name = file_name.replace('\'', '_');
    	file_name = file_name.replace('>',  '_');
    	file_name = file_name.replace('<',  '_');
    	file_name = file_name.replace('[',  '_');
    	file_name = file_name.replace(']',  '_');
    	file_name = file_name.replace('|',  '_');
    	
    	file_name = file_name + "." + extension;
    
    	Bundle downloader_params = new Bundle();

    	downloader_params.putString("url",         url);
    	downloader_params.putString("output_dir",  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
    	downloader_params.putString("output_file", file_name);
    	
		ShowVideoProgressDialog();

		if (videoDownloader == null) {
			videoDownloader = new VideoDownloader(this); 
			videoDownloader.execute(downloader_params);
		}
}
    
    @Override
    public void onMetadataDownloadComplete(String metadata) {
    	metadataDownloader = null;
    	
    	DismissDialog();

    	if (metadata != null) {
    		Uri metadata_uri = Uri.parse("http://localhost/?" + metadata);
    		
    		String video_title = metadata_uri.getQueryParameter("title");
    		
    		if (video_title == null) {
    			video_title = "video";
    		}
    		
    		String url_encoded_fmt_stream_map = metadata_uri.getQueryParameter("url_encoded_fmt_stream_map");
    		
    		if (url_encoded_fmt_stream_map != null) {
    			ArrayList<String> itags      = new ArrayList<String>();
    			ArrayList<String> formats    = new ArrayList<String>();
    			ArrayList<String> extensions = new ArrayList<String>();
    			ArrayList<String> urls       = new ArrayList<String>();
    			
    			String[] splitted = url_encoded_fmt_stream_map.split(",");
    			
    			for (int i = 0; i < splitted.length; i++) {
    				Uri item_uri = Uri.parse("http://localhost/?" + splitted[i]);
    				
    				String itag = item_uri.getQueryParameter("itag");
    				String url  = item_uri.getQueryParameter("url");
    				String sig  = item_uri.getQueryParameter("sig");

    				if (itag != null && url != null) {
        				Uri url_uri = Uri.parse(url);
        				
        				if (url_uri.getQueryParameter("signature") == null && sig != null) {
        					try {
        						url = url + "&signature=" + URLEncoder.encode(sig, "UTF-8");
        					} catch (Exception ex) {
        						// Ignored
        					}
        				}
        				
        				if (itag.equals("22")) {
        					itags.add     (itag);
        					formats.add   ("MP4 (H.264 720p HD)");
        					extensions.add("mp4");
        					urls.add      (url);
        				} else if (itag.equals("18")) {
        					itags.add     (itag);
        					formats.add   ("MP4 (H.264 360p)");
        					extensions.add("mp4");
        					urls.add      (url);
        				} else if (itag.equals("5")) {
        					itags.add     (itag);
        					formats.add   ("FLV (H.263 240p)");
        					extensions.add("flv");
        					urls.add      (url);
        				} else if (itag.equals("36")) {
        					itags.add     (itag);
        					formats.add   ("3GP (MPEG-4 240p)");
        					extensions.add("3gp");
        					urls.add      (url);
        				} else if (itag.equals("17")) {
        					itags.add     (itag);
        					formats.add   ("3GP (MPEG-4 144p)");
        					extensions.add("3gp");
        					urls.add      (url);
        				}
    				}
    			}
    			
    			if (itags.size() != 0) {
    				ShowFormatSelectionDialog(video_title, itags, formats, extensions, urls);
    			} else {
        			ShowMessageDialog(getString(R.string.dialog_title_error), getString(R.string.dialog_message_no_valid_formats));
    			}
    		} else {
    			ShowMessageDialog(getString(R.string.dialog_title_error), getString(R.string.dialog_message_no_valid_formats));
    		}
    	} else {
    		ShowMessageDialog(getString(R.string.dialog_title_info), getString(R.string.dialog_message_operation_cancelled));
    	}
    }
    
    public void onVideoDownloadProgressUpdate(int percent) {
    	DialogFragment fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (fragment != null && (ProgressDialog)fragment.getDialog() != null) {
			((ProgressDialog)fragment.getDialog()).setProgress(percent);
    	}
    }
    
    @Override
    public void onVideoDownloadComplete(String error_message) {
    	videoDownloader = null;
    	
    	DismissDialog();
    	
    	if (error_message != null) {
    		if (error_message.equals("")) {
        		ShowMessageDialog(getString(R.string.dialog_title_info), getString(R.string.dialog_message_download_complete));
    		} else {
    			ShowMessageDialog(getString(R.string.dialog_title_error), error_message);
    		}
    	} else {
    		ShowMessageDialog(getString(R.string.dialog_title_info), getString(R.string.dialog_message_operation_cancelled));
    	}
    }
    
    private void ShowMessageDialog(String title, String message) {
    	DialogFragment prev_fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (prev_fragment != null) {
    		prev_fragment.dismiss();
    	}
    	
    	DialogFragment fragment = CustomDialogFragment.newMessageInstance(title, message);
    	
    	fragment.show(getFragmentManager(), "dialog");
    }

    private void ShowMetadataProgressDialog() {
    	DialogFragment prev_fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (prev_fragment != null) {
    		prev_fragment.dismiss();
    	}
    	
    	DialogFragment fragment = CustomDialogFragment.newMetadataProgressInstance();
    	
    	fragment.show(getFragmentManager(), "dialog");
    }

    private void ShowVideoProgressDialog() {
    	DialogFragment prev_fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (prev_fragment != null) {
    		prev_fragment.dismiss();
    	}
    	
    	DialogFragment fragment = CustomDialogFragment.newVideoProgressInstance();
    	
    	fragment.show(getFragmentManager(), "dialog");
    }

    private void ShowFormatSelectionDialog(String video_title, ArrayList<String> itags, ArrayList<String> formats, ArrayList<String> extensions, ArrayList<String> urls) {
    	DialogFragment prev_fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (prev_fragment != null) {
    		prev_fragment.dismiss();
    	}
    	
    	DialogFragment fragment = CustomDialogFragment.newFormatSelectionInstance(video_title, getPreferences(MODE_PRIVATE).getString("PreferredITag", ""), itags, formats, extensions, urls);
    	
    	fragment.show(getFragmentManager(), "dialog");
    }
    
    private void DismissDialog() {
    	DialogFragment fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (fragment != null) {
    		fragment.dismiss();
    	}
    }
    
    public static class MainFragment extends Fragment {
        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View root_view = inflater.inflate(R.layout.fragment_main, container, false);

            WebView     web_view     = (WebView)root_view.findViewById(R.id.webview);
            WebSettings web_settings = web_view.getSettings();
            
            web_settings.setJavaScriptEnabled(true);
            
            web_view.loadUrl("http://m.youtube.com/");

            return root_view;
        }
    }
}
