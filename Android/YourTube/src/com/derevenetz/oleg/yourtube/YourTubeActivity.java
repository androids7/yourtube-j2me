package com.derevenetz.oleg.yourtube;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.json.JSONObject;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.nokia.payment.iap.aidl.INokiaIAPService;
import com.derevenetz.oleg.yourtube.BuildSettings;
import com.derevenetz.oleg.yourtube.CustomDialogFragment;
import com.derevenetz.oleg.yourtube.CustomDialogFragment.CustomDialogFragmentListener;
import com.derevenetz.oleg.yourtube.MetadataDownloader;
import com.derevenetz.oleg.yourtube.MetadataDownloader.MetadataDownloaderListener;

public class YourTubeActivity extends Activity implements MetadataDownloaderListener, CustomDialogFragmentListener {
    private final int                          MAX_FREE_DOWNLOAD_ATTEMPTS  = 3,
                                               IAP_RESULT_OK               = 0,
                                               REQUEST_CODE_BUY_INTENT     = 1000;
    
    private final String                       IAP_FULL_VERSION_PRODUCT_ID = "1258455",
                                               IAP_DEVELOPER_PAYLOAD       = "PXV0HzqSbr1ZTg0XoJX6a2hUZp6xFroR";
    
    private boolean                            nokiaIAPSupported           = false,
                                               isFullVersion               = false,
                                               isOptionsMenuEnabled        = true;
    private FrameLayout                        activityContentView         = null;
    private View                               webCustomView               = null;
    private FrameLayout                        webCustomViewContainer      = null;
    private WebChromeClient.CustomViewCallback webCustomViewCallback       = null;
    private WebChromeClient                    webChromeClient             = null;
    private MetadataDownloader                 metadataDownloader          = null;
    private INokiaIAPService                   nokiaIAPService             = null;
    
    private ServiceConnection nokiaIAPServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            nokiaIAPService = INokiaIAPService.Stub.asInterface(service);
            
            try {
                int response = nokiaIAPService.isBillingSupported(3, getPackageName(), "inapp");
                
                if (response == IAP_RESULT_OK) {
                    nokiaIAPSupported = true;
                    
                    (new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<String> product_list   = new ArrayList<String>();
                            Bundle            query_products = new Bundle();
                            
                            product_list.add(IAP_FULL_VERSION_PRODUCT_ID);
                            
                            query_products.putStringArrayList("ITEM_ID_LIST", product_list);
                            
                            String continuationToken = null;
                            
                            do {
                                try {
                                    Bundle owned_items = nokiaIAPService.getPurchases(3, getPackageName(), "inapp", query_products, continuationToken);
                                    
                                    if (owned_items.getInt("RESPONSE_CODE", -1) == IAP_RESULT_OK) {
                                        ArrayList<String> owned_products = owned_items.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                                        
                                        for (int i = 0; i < owned_products.size(); i++) {
                                            String product = owned_products.get(i);
                                            
                                            if (product.equals(IAP_FULL_VERSION_PRODUCT_ID)) {
                                                isFullVersion = true;

                                                SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                                                
                                                editor.putBoolean("FullVersion", isFullVersion);
                                                editor.commit();
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    continuationToken = null;
                                }
                            } while (!TextUtils.isEmpty(continuationToken));
                        }
                    })).start();
                } else {
                    nokiaIAPSupported = false;
                }
            } catch (Exception ex) {
                nokiaIAPSupported = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            nokiaIAPSupported = false;
            nokiaIAPService   = null;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        
        setContentView(R.layout.activity_yourtube);

        WebView web_view = (WebView)findViewById(R.id.webview);

        if (web_view != null) {
            web_view.getSettings().setJavaScriptEnabled(true);
            web_view.setWebViewClient(new WebViewClient());
            
            webChromeClient = new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int progress) {
                    if (getWindow().hasFeature(Window.FEATURE_PROGRESS)) {
                        setProgress(progress * 100);
                    }
                }

                @Override
                public void onShowCustomView(View view, CustomViewCallback callback) {
                    if (webCustomView != null) {
                        callback.onCustomViewHidden();
                    } else {
                        activityContentView = (FrameLayout)findViewById(R.id.activity_yourtube);
                        activityContentView.setVisibility(View.GONE);

                        view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                          FrameLayout.LayoutParams.MATCH_PARENT));

                        webCustomView         = view;
                        webCustomViewCallback = callback;

                        webCustomViewContainer = new FrameLayout(YourTubeActivity.this);
                        webCustomViewContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                                            FrameLayout.LayoutParams.MATCH_PARENT));
                        webCustomViewContainer.setBackgroundResource(android.R.color.black);
                        webCustomViewContainer.addView(view);
                        webCustomViewContainer.setVisibility(View.VISIBLE);
                        
                        setContentView(webCustomViewContainer);

                        isOptionsMenuEnabled = false;
                        
                        invalidateOptionsMenu();
                    }
                }
                
                @Override
                public void onHideCustomView() {
                    if (webCustomView != null) {
                        webCustomView.setVisibility(View.GONE);
                        
                        webCustomViewContainer.removeView(webCustomView);
                        webCustomViewContainer.setVisibility(View.GONE);
                        
                        webCustomViewCallback.onCustomViewHidden();
                        
                        activityContentView.setVisibility(View.VISIBLE);
                        
                        setContentView(activityContentView);

                        isOptionsMenuEnabled = true;

                        invalidateOptionsMenu();

                        activityContentView    = null;
                        webCustomView          = null;
                        webCustomViewContainer = null;
                        webCustomViewCallback  = null;
                    }
                }

                @Override
                public View getVideoLoadingProgressView() {
                    FrameLayout layout = new FrameLayout(YourTubeActivity.this);
                    
                    layout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                        FrameLayout.LayoutParams.MATCH_PARENT));
                    webCustomViewContainer.setBackgroundResource(android.R.color.black);
                    
                    return layout;
                }
            };

            web_view.setWebChromeClient(webChromeClient);

            if (savedInstanceState != null) {
                web_view.restoreState(savedInstanceState);
            } else {
                web_view.loadUrl("http://m.youtube.com/");
            }
        }

        if (BuildSettings.BUILD_FOR_NOKIA_STORE) {
            isFullVersion = getPreferences(MODE_PRIVATE).getBoolean("FullVersion", false);
            
            bindService(new Intent("com.nokia.payment.iapenabler.InAppBillingService.BIND"), nokiaIAPServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            isFullVersion = true;
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

        WebView web_view = (WebView)findViewById(R.id.webview);

        if (web_view != null) {
            web_view.saveState(outState);
        } else if (activityContentView != null) {
            web_view = (WebView)activityContentView.findViewById(R.id.webview);
            
            if (web_view != null) {
                web_view.saveState(outState);
            }
        }

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
    public void onPause() {
        super.onPause();

        WebView web_view = (WebView)findViewById(R.id.webview);

        if (web_view != null) {
            web_view.onPause();
        } else if (activityContentView != null) {
            web_view = (WebView)activityContentView.findViewById(R.id.webview);
            
            if (web_view != null) {
                web_view.onPause();
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();

        WebView web_view = (WebView)findViewById(R.id.webview);

        if (web_view != null) {
            web_view.onResume();
        } else if (activityContentView != null) {
            web_view = (WebView)activityContentView.findViewById(R.id.webview);
            
            if (web_view != null) {
                web_view.onResume();
            }
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
        
        if (nokiaIAPService != null) {
            unbindService(nokiaIAPServiceConnection);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_yourtube, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        if (isOptionsMenuEnabled) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_download) {
            int download_attempt = getPreferences(MODE_PRIVATE).getInt("DownloadAttempt", 0);
            
            if (isFullVersion || download_attempt < MAX_FREE_DOWNLOAD_ATTEMPTS) {
                if (!isFullVersion) {
                    showToast(String.format(getString(R.string.toast_message_trial_mode),
                                            Integer.toString(MAX_FREE_DOWNLOAD_ATTEMPTS - download_attempt)));
                }
                
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
                                                                                                            .appendQueryParameter("el",       "detailpage")
                                                                                                            .appendQueryParameter("ps",       "default")
                                                                                                            .appendQueryParameter("eurl",     "")
                                                                                                            .appendQueryParameter("gl",       "US")
                                                                                                            .appendQueryParameter("hl",       "en");
                            
                            if (metadataDownloader == null) {
                                showMetadataProgressDialog();

                                metadataDownloader = new MetadataDownloader(builder.build().toString(), this);
                                metadataDownloader.execute(builder.build().toString());
                            }
                        }
                    }
                }
            } else {
                showBuyFullVersionQuestionDialog();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CODE_BUY_INTENT) {
            if (data.getIntExtra("RESPONSE_CODE", -1) == IAP_RESULT_OK) {
                try {
                    JSONObject object = new JSONObject(data.getStringExtra("INAPP_PURCHASE_DATA"));
                    
                    if (object.getString("productId").equals(IAP_FULL_VERSION_PRODUCT_ID) &&
                        object.getString("developerPayload").equals(IAP_DEVELOPER_PAYLOAD)) {
                        isFullVersion = true;

                        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
                        
                        editor.putBoolean("FullVersion", isFullVersion);
                        editor.commit();
                    }
                } catch (Exception ex) {
                    showToast(getString(R.string.toast_message_purchase_failed));
                }
            } else {
                showToast(getString(R.string.toast_message_purchase_failed));
            }
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        WebView web_view = (WebView)findViewById(R.id.webview);

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webCustomView != null && webChromeClient != null) {
                webChromeClient.onHideCustomView();
                
                return true;
            } else if (web_view != null && web_view.canGoBack() && !(web_view.getUrl().equals("http://m.youtube.com/") &&
                                                                     web_view.copyBackForwardList().getCurrentIndex() <= 1)) {
                web_view.goBack();
                
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onBuyFullVersionAgree() {
        if (nokiaIAPSupported) {
            try {
                Bundle        intent_bundle  = nokiaIAPService.getBuyIntent(3, getPackageName(), IAP_FULL_VERSION_PRODUCT_ID, "inapp", IAP_DEVELOPER_PAYLOAD);
                PendingIntent pending_intent = intent_bundle.getParcelable("BUY_INTENT");
                
                startIntentSenderForResult(pending_intent.getIntentSender(), REQUEST_CODE_BUY_INTENT, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
            } catch (Exception ex) {
                showToast(getString(R.string.toast_message_purchase_failed)); 
            }
        } else {
            showToast(getString(R.string.toast_message_iap_not_supported));
        }
    }
    
    @Override
    public void onMetadataProgressCancelled() {
        if (metadataDownloader != null) {
            metadataDownloader.cancel(true);
        }
    }
    
    @Override
    public void onFormatSelected(String video_title, String itag, String extension, String url, boolean is_worst_format) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

        if (isFullVersion || is_worst_format) {
            if (!isFullVersion) {
                editor.putInt("DownloadAttempt", getPreferences(MODE_PRIVATE).getInt("DownloadAttempt", 0) + 1);
            }
            
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
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    try {
                        Request request = new Request(Uri.parse(url));

                        request.setDescription(file_name);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file_name);
                        request.allowScanningByMediaScanner();

                        manager.enqueue(request);

                        showToast(getString(R.string.toast_message_download_started));
                    } catch (Exception ex) {
                        showToast(getString(R.string.toast_message_download_failed));
                    }
                } else {
                    showToast(getString(R.string.toast_message_media_unavailable));
                }
            } else {
                showToast(getString(R.string.toast_message_download_failed));
            }
        } else {
            showBuyFullVersionQuestionDialog();
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
                
                HashMap<String, String> itag_to_url = new HashMap<String, String>();
                
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
                        
                        itag_to_url.put(itag, url);
                    }
                }
                
                if (itag_to_url.containsKey("22")) {
                    itags.add     ("22");
                    formats.add   ("MP4 (H.264 720p HD)");
                    extensions.add("mp4");
                    urls.add      (itag_to_url.get("22"));
                } 
                if (itag_to_url.containsKey("18")) {
                    itags.add     ("18");
                    formats.add   ("MP4 (H.264 360p)");
                    extensions.add("mp4");
                    urls.add      (itag_to_url.get("18"));
                }
                if (itag_to_url.containsKey("5")) {
                    itags.add     ("5");
                    formats.add   ("FLV (H.263 240p)");
                    extensions.add("flv");
                    urls.add      (itag_to_url.get("5"));
                }
                if (itag_to_url.containsKey("36")) {
                    itags.add     ("36");
                    formats.add   ("3GP (MPEG-4 240p)");
                    extensions.add("3gp");
                    urls.add      (itag_to_url.get("36"));
                }
                if (itag_to_url.containsKey("17")) {
                    itags.add     ("17");
                    formats.add   ("3GP (MPEG-4 144p)");
                    extensions.add("3gp");
                    urls.add      (itag_to_url.get("17"));
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
    
    private void showBuyFullVersionQuestionDialog() {
        DialogFragment prev_fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
        
        if (prev_fragment != null) {
            prev_fragment.dismiss();
        }
        
        DialogFragment fragment = CustomDialogFragment.newBuyFullVersionQuestionInstance();
        
        fragment.show(getFragmentManager(), "dialog");
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

        if (isFullVersion) {
            DialogFragment fragment = CustomDialogFragment.newFormatSelectionInstance(video_title, getPreferences(MODE_PRIVATE).getString("PreferredITag", ""), itags, formats, extensions, urls, false);
            
            fragment.show(getFragmentManager(), "dialog");
        } else {
            DialogFragment fragment = CustomDialogFragment.newFormatSelectionInstance(video_title, getPreferences(MODE_PRIVATE).getString("PreferredITag", ""), itags, formats, extensions, urls, true);
            
            fragment.show(getFragmentManager(), "dialog");
        }
    }
    
    private void dismissDialog() {
        DialogFragment fragment = (DialogFragment)getFragmentManager().findFragmentByTag("dialog");
        
        if (fragment != null) {
            fragment.dismiss();
        }
    }
}
