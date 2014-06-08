package com.derevenetz.oleg.yourtube;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.Fragment;
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
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.derevenetz.oleg.yourtube.CustomDialogFragment;
import com.derevenetz.oleg.yourtube.CustomDialogFragment.CustomDialogFragmentListener;
import com.derevenetz.oleg.yourtube.MetadataDownloader;
import com.derevenetz.oleg.yourtube.MetadataDownloader.MetadataDownloaderListener;

public class YourTubeActivity extends Activity implements MetadataDownloaderListener, CustomDialogFragmentListener {
	private MetadataDownloader metadataDownloader = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        
        setContentView(R.layout.activity_yourtube);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.activity_yourtube, new MainFragment()).commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    	
    	if (savedInstanceState.getBoolean("YourTubeActivity.metadataDownloaderRunning", false)) {
			if (metadataDownloader == null) {
				String url = savedInstanceState.getString ("YourTubeActivity.metadataDownloaderURL", "");
				
				showMetadataProgressDialog();

				metadataDownloader = new MetadataDownloader(url, this);
				metadataDownloader.execute(url);
			}
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if (metadataDownloader != null) {
    		dismissDialog();
    	}
    	
    	super.onSaveInstanceState(outState);
    	
    	if (metadataDownloader != null) {
    		outState.putBoolean("YourTubeActivity.metadataDownloaderRunning", true);
    		outState.putString ("YourTubeActivity.metadataDownloaderURL",     metadataDownloader.getUrl());

			metadataDownloader.setListener(null);
    		metadataDownloader.cancel(true);
    		
    		metadataDownloader = null;
    	} else {
    		outState.putBoolean("YourTubeActivity.metadataDownloaderRunning", false);
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
		if (metadataDownloader != null) {
			metadataDownloader.setListener(null);
    		metadataDownloader.cancel(true);
    		
    		metadataDownloader = null;
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
        				
        				if (metadataDownloader == null) {
            				showMetadataProgressDialog();

            				metadataDownloader = new MetadataDownloader(builder.build().toString(), this);
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
    
		DownloadManager manager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		
		if (manager != null) {
			Request request = new Request(Uri.parse(url));

			request.setDescription(file_name);
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file_name);
			request.allowScanningByMediaScanner();
			
			try {
				manager.enqueue(request);

				showToast(getString(R.string.toast_message_download_started));
			} catch (Exception ex) {
				showToast(getString(R.string.toast_message_download_failed));
			}
		} else {
			showToast(getString(R.string.toast_message_download_failed));
		}
    }
    
    @Override
    public void onMetadataDownloadComplete(String metadata) {
    	metadataDownloader = null;
    	
    	dismissDialog();

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
    				showFormatSelectionDialog(video_title, itags, formats, extensions, urls);
    			} else {
    				showToast(getString(R.string.toast_message_no_valid_formats));
    			}
    		} else {
				showToast(getString(R.string.toast_message_no_valid_formats));
    		}
    	} else {
    		showToast(getString(R.string.toast_message_operation_cancelled));
    	}
    }
    
    private void showToast(String message) {
    	Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void showMetadataProgressDialog() {
    	DialogFragment prev_fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (prev_fragment != null) {
    		prev_fragment.dismiss();
    	}
    	
    	DialogFragment fragment = CustomDialogFragment.newMetadataProgressInstance();
    	
    	fragment.show(getFragmentManager(), "dialog");
    }

    private void showFormatSelectionDialog(String video_title, ArrayList<String> itags, ArrayList<String> formats, ArrayList<String> extensions, ArrayList<String> urls) {
    	DialogFragment prev_fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
    	
    	if (prev_fragment != null) {
    		prev_fragment.dismiss();
    	}
    	
    	DialogFragment fragment = CustomDialogFragment.newFormatSelectionInstance(video_title, getPreferences(MODE_PRIVATE).getString("PreferredITag", ""), itags, formats, extensions, urls);
    	
    	fragment.show(getFragmentManager(), "dialog");
    }
    
    private void dismissDialog() {
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
            View    root_view = inflater.inflate(R.layout.fragment_main, container, false);
            WebView web_view  = (WebView)root_view.findViewById(R.id.webview);

            web_view.getSettings().setJavaScriptEnabled(true);
        	web_view.setWebViewClient(new WebViewClient());

            if (getActivity() != null && getActivity().getWindow().hasFeature(Window.FEATURE_PROGRESS)) {
            	web_view.setWebChromeClient(new WebChromeClient() {
            		public void onProgressChanged(WebView view, int progress) {
            			if (getActivity() != null) {
            				getActivity().setProgress(progress * 100);
            			}
            		}
            	});
            } else {
            	web_view.setWebChromeClient(new WebChromeClient());
            }
        	
            if (savedInstanceState != null) {
            	web_view.restoreState(savedInstanceState);
            } else {
                web_view.loadUrl("http://m.youtube.com/");
            }

        	return root_view;
        }
        
        @Override
        public void onSaveInstanceState(Bundle outState) {
        	super.onSaveInstanceState(outState);
        	
        	((WebView)getView().findViewById(R.id.webview)).saveState(outState);
        }
    }
}
