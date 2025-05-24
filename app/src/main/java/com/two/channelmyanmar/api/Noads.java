package com.two.channelmyanmar.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Noads implements Runnable {

    private static final String NOADS_BASE_URL = "https://noads.leetdev.net/"; // Define base URL as constant
    private static final int LINK_FIND_INITIAL_DELAY = 100; // Initial delay for JS execution
    private static final int LINK_FIND_POLL_INTERVAL = 2000; // Polling interval to find link
    private static final int LINK_FIND_TIMEOUT_SEC = 40; // Timeout after which consider link not found

    private String link;

    private Context context;
    private Handler handler = new Handler(Looper.getMainLooper()); // Use MainLooper Handler directly
    private boolean found = false;
    private int sec = 0;
    ExtractorListener cb;
    WebView webView;
    public Noads(Context webView, String url, ExtractorListener cb) {
        this.cb = cb;
        this.link = url;
        this.context = webView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        try {
            if (context == null) {
                throw new IllegalStateException("WebView instance is null. Ensure WebView is properly initialized and passed to Noads.");
            }
            webView = new WebView(context);

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setDatabaseEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setAllowFileAccess(false); // Consider security: Disable file access unless strictly needed
            webSettings.setGeolocationEnabled(true);
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

            webView.addJavascriptInterface(new WebAppInterface(), "Android");
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);


            webView.setWebViewClient(new CustomWebViewClient());
            webView.setWebChromeClient(new CustomWebChromeClient());


        } catch (Exception e) {
            Log.e("Noads", "Error setting up WebView: " + e.getMessage());
            cb.onFailed("WebView setup error: " + e.getMessage());
        }
    }
    public void destroy() {
        destroyWebViewOnUiThread("Error");
    }


    @Override
    public void run() {
        handler.post(() -> { // Post WebView setup and loading to main looper
            try {
                setupWebView();
                webView.loadUrl(NOADS_BASE_URL); // Load base URL from constant
            } catch (Exception e) {
                Log.e("Noads", "Runnable execution error: " + e.getMessage());
                destroyWebViewOnUiThread("Task execution error: " + e.getMessage());
                /// cb.onError("Task execution error: " + e.getMessage());
            }
        });
    }


    private class CustomWebViewClient extends WebViewClient {



        @Override
        public void onPageFinished(final WebView wv, String url) {
            // Inject link after page is finished loading with an initial delay


            if (Noads.this.link.contains("drive.google.com")) {
                // Google Drive specific JavaScript execution
                wv.evaluateJavascript("javascript:var id=document.getElementById('uc-download-link');if(id){id.click();}else{var link=window.location.href;Android.onFail('Sorry,Too many users have viewed or downloaded this file recently.Please try accessing the file again later.Plz Open in drive.',link);}", null);
            }
        }
    }

    private class CustomWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d("Noads", "Loading Progress: " + newProgress + "%");
            cb.loading(newProgress);
            if(newProgress == 100){
                handler.postDelayed(() -> injectLinkAndSubmitForm(view), LINK_FIND_INITIAL_DELAY);
                // Start polling to find the link

            }
            super.onProgressChanged(view, newProgress);
        }
    }


    private void injectLinkAndSubmitForm(WebView wv) {
        String jsCode = "var inputField = document.querySelector('input[name=\"link\"]'); " +
                "if (inputField) { " +
                "   inputField.value = '" + link + "'; " +
                "} " +
                "var form = document.getElementById('myform'); " +
                "if (form) { " +
                "   do_form($('#myform').serialize()); " +
                "} else { " +
                "   console.error('Form not found.'); " +
                "}";
        wv.evaluateJavascript(jsCode, null);
        startLinkFindPolling(wv);
        Log.d("Noads", "JavaScript for link injection and form submission executed.");
    }


    private void startLinkFindPolling(final WebView wv) {
        final Runnable pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!found) {
                    findLink(wv);
                    if (sec < LINK_FIND_TIMEOUT_SEC) { // Check against timeout constant
                        handler.postDelayed(this, LINK_FIND_POLL_INTERVAL); // Use poll interval constant
                        sec += (LINK_FIND_POLL_INTERVAL / 1000); // Increment seconds based on poll interval
                    } else {
                        if (!found) { // Double check found flag before error callback after timeout
                            // cb.onError("Timeout: Unable to extract link after " + LINK_FIND_TIMEOUT_SEC + " seconds.");
                            destroyWebViewOnUiThread("Timeout: Unable to extract link after " + LINK_FIND_TIMEOUT_SEC + " seconds.");
                        }
                    }
                }
            }
        };
        handler.postDelayed(pollingRunnable, LINK_FIND_POLL_INTERVAL); // Start polling with interval constant
    }


    public void findLink(WebView wv) {
        handler.post(() -> {
            Log.d("Noads", "Executing JavaScript to find link...");
            wv.evaluateJavascript(
                    "javascript:var resultDiv = document.getElementById('result'); " +
                            "if (resultDiv && window.getComputedStyle(resultDiv).getPropertyValue('display') !== 'none') { " +
                            "  resultDiv.getElementsByTagName('a')[0].getAttribute('href'); " +
                            "} else { " +
                            "  'Error: Sorry we can not extract link this time!'; " +
                            "}",
                    value -> {
                        String result = String.valueOf(value).replace("\"", "");
                        if (result.contains("Error")) {
                            String errorMessage = result.substring("Error: ".length());
                            Log.d("Noads", "Link extraction error: " + errorMessage);
                            // Error callback already handled in timeout mechanism, avoid duplicate error call.
                            // if (sec > LINK_FIND_TIMEOUT_SEC) { cb.onError(errorMessage); } // Removed duplicate error call
                        } else if (!"null".equals(result)){ // Check for non-null result
                            Log.d("Noads", "Final link found: " + result);
                            if (result.contains("drive")){
                                webView.loadUrl(result);
                                sec=0;
                            }else {
                                found = true;
                                cb.onSuccessMegaUp(result);
                                destroyWebViewOnUiThread("");
                            }
                        } else {
                            Log.d("Noads", "Result is null, link not found yet.");
                        }
                    });
        });
    }


    private void destroyWebViewOnUiThread(String msg) {
        handler.post(this::destroyWebView);
        if (cb!=null && !msg.isEmpty())
            cb.onFailed(msg);
    }



    private void destroyWebView() {
        if (webView != null) {
            webView.stopLoading();
            webView.loadUrl("about:blank");
            webView.pauseTimers();
            webView.clearHistory();
            webView.clearCache(true);
            webView.clearFormData();
            webView.destroyDrawingCache();
            webView.removeAllViews();
            webView.destroy();
            webView = null;
            Log.d("Noads", "WebView destroyed.");
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onSuccess(String response) {
            // Called from Javascript when link is successfully extracted within the web page itself.
            // This is another pathway for success, in addition to polling mechanism.
            cb.onSuccessMegaUp(response);
            found = true; // Ensure polling stops if this success callback is triggered.
            Log.d("Noads", "JavaScript onSuccess callback received: " + response);
            destroyWebViewOnUiThread("");
        }

        @JavascriptInterface
        public void onFail(String response, String link) { // Pass back the link for potential error context
            // cb.onError("Link extraction failed from webpage: " + response + ". URL attempted: " + link);
            cb.onSuccessYoteShin(link,false);
            Log.e("Noads", "JavaScript onFail callback received: " + response + ", URL: " + link);
            destroyWebViewOnUiThread("Link extraction failed from webpage: " + response + ". URL attempted: " + link);
        }

        @JavascriptInterface
        public void onFail(String response) { // Overloaded method in case link is not easily available in JS context
            //cb.onError("Link extraction failed from webpage: " + response );
            Log.e("Noads", "JavaScript onFail callback received: " + response);
            destroyWebViewOnUiThread("Link extraction failed from webpage: " + response);
        }
    }
}
