package com.two.channelmyanmar.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.*;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NoAds implements Runnable {
    private static final String TAG = "NoAds";
    private static final int POLL_INTERVAL = 1000; // 2 seconds
    private static final int TIMEOUT = 15000; // 15 seconds

    private final WeakReference<WebView> webViewRef;
    private final String targetUrl;
    private final WeakReference<Callback> callbackRef;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<Runnable> pendingTasks = new CopyOnWriteArrayList<>();

    private volatile boolean isDestroyed = false;
    private int elapsedSeconds = 0;
    private boolean isLinkFound = false;

    public interface Callback {
        void onSuccess(String url);
        void onError(String message);
    }

    public NoAds(WebView webView, String url, Callback callback) {
        this.webViewRef = new WeakReference<>(webView);
        this.targetUrl = sanitizeUrl(url);
        this.callbackRef = new WeakReference<>(callback);
    }

    @Override
    public void run() {
        if (isDestroyed) return;
        mainHandler.post(() -> {
            if (isDestroyed || !isWebViewValid()) return;
            initializeWebView();
        });
    }

    public void destroy() {
        isDestroyed = true;
        cleanupResources();
    }

    private boolean isWebViewValid() {
        WebView webView = webViewRef.get();
        return webView != null && webView.getContext() != null &&
                !((Context) webView.getContext()).isRestricted();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeWebView() {
        try {
            if (isDestroyed || !isWebViewValid()) return;
            WebView webView = webViewRef.get();
            if (webView == null) return;

            // Configure WebView settings
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

            // Set up WebView clients
            webView.setWebViewClient(new CustomWebViewClient());
            webView.setWebChromeClient(new CustomWebChromeClient());

            // Configure cookies
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setAcceptThirdPartyCookies(webView, true);

            // Inject JavaScript interface
            webView.addJavascriptInterface(new WebAppInterface(), "Android");

            // Load the initial URL
            webView.loadUrl("https://noads.leetdev.net/");
        } catch (Exception e) {
            safeError("Initialization failed: " + e.getMessage());
            cleanupResources();
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            checkGoogleDriveSpecialCase(url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            safeError("Page load error: " + error.getDescription());
        }
    }

    private class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.d(TAG, "Loading progress: " + newProgress + "%");
            if (newProgress == 100){
                if (isDestroyed) return;
                scheduleFormSubmission();
                startLinkDetection();

            }
        }
    }

    private void scheduleFormSubmission() {
        Runnable submissionTask = () -> {
            if (isDestroyed || !isWebViewValid()) return;

            String sanitizedUrl = targetUrl.replace("'", "\\'");
            String jsCode = "(function() {" +
                    "var inputField = document.querySelector('input[name=\"link\"]');" +
                    "if (inputField) { inputField.value = '" + sanitizedUrl + "'; }" +
                    "var form = document.getElementById('myform');" +
                    "if (form) { do_form($('#myform').serialize()); }" +
                    "})();";

            safeEvaluateJavascript(jsCode, null);
        };

        postDelayedTask(submissionTask, 100);
    }

    private void startLinkDetection() {
        Runnable detectionTask = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed || isLinkFound) return;

                if (elapsedSeconds >= TIMEOUT / 1000) {
                    handleDetectionTimeout();
                    return;
                }
                detectDownloadLink();
                scheduleNextDetection();
            }
        };

        postDelayedTask(detectionTask, POLL_INTERVAL);
    }

    private void scheduleNextDetection() {
        if (isDestroyed) return;
        elapsedSeconds += POLL_INTERVAL / 1000;
        postDelayedTask(this::startLinkDetection, POLL_INTERVAL);
    }

    private void detectDownloadLink() {
        if (!isWebViewValid()) return;
        Log.d("Noads","Detecting");

        String detectionScript = "(function() {" +
                "try {" +
                "  var resultDiv = document.getElementById('result');" +
                "  if (resultDiv && window.getComputedStyle(resultDiv).display !== 'none') {" +
                "    var link = resultDiv.getElementsByTagName('a')[0];" +
                "    return link ? link.href : 'Error: No download link found';" +
                "  }" +
                "  return 'Error: Result container not visible';" +
                "} catch(e) { return 'Error: ' + e.message; }" +
                "})()";

        safeEvaluateJavascript(detectionScript, value -> {
            if (isDestroyed) return;

            String result = value != null ? value.replaceAll("^\"|\"$", "") : "";
            if (result.contains("Error")) {
                Log.w(TAG, "Detection error: " + result);
            } else {
                isLinkFound = true;
                safeSuccess(result);
                cleanupResources();
            }
        });
    }

    private void checkGoogleDriveSpecialCase(String url) {
        if (url.contains("drive.google.com")) {
            String driveScript = "javascript:(function() {" +
                    "var downloadLink = document.getElementById('uc-download-link');" +
                    "if (downloadLink) { downloadLink.click(); }" +
                    "else { Android.onFail('Drive download unavailable'); }" +
                    "})()";

            safeEvaluateJavascript(driveScript, null);
        }
    }

    private void handleDetectionTimeout() {
        safeError("Operation timed out");
        cleanupResources();
    }

    private void safeEvaluateJavascript(String script, ValueCallback<String> callback) {
        WebView webView = webViewRef.get();
        if (webView != null && !isDestroyed) {
            webView.evaluateJavascript(script, callback);
        } else {
            safeError("WebView not available for JavaScript evaluation");
        }
    }

    private void postDelayedTask(Runnable task, long delay) {
        if (isDestroyed) return;

        pendingTasks.add(task);
        mainHandler.postDelayed(() -> {
            pendingTasks.remove(task);
            if (!isDestroyed) task.run();
        }, delay);
    }

    private void cleanupResources() {
        isDestroyed = true;
        //mainHandler.removeCallbacksAndMessages(null);
        pendingTasks.clear();
        if(mainHandler == null)
            return;
        mainHandler.post(() -> {
            WebView webView = webViewRef.get();
            if (webView != null) {
                try {
                    // Stop loading and clear WebView state
                    webView.stopLoading();
                    webView.loadUrl("about:blank");
                    webView.onPause();
                    webView.pauseTimers();
                    webView.clearHistory();
                    webView.clearCache(true);
                    webView.clearFormData();
                    webView.destroyDrawingCache();


                    // Detach WebView from its parent
                    if (webView.getParent() instanceof ViewGroup) {
                        ((ViewGroup) webView.getParent()).removeView(webView);
                    }

                    // Destroy the WebView
                    webView.removeAllViews();
                    webView.destroy();
                    webView=null;
                } catch (Exception e) {
                    Log.e(TAG, "Cleanup error", e);
                    safeError(e.toString());
                }
                safeError("Error");
                webViewRef.clear();
            }
        });
    }

    private void safeSuccess(String result) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            try {
                callback.onSuccess(result);
            } catch (Exception e) {
                Log.e(TAG, "Callback onSuccess error", e);
            }
        }
    }

    private void safeError(String message) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            try {
                callback.onError(message);
            } catch (Exception e) {
                Log.e(TAG, "Callback onError error", e);
            }
        }
        cleanupResources();
    }

    private static String sanitizeUrl(String url) {
        if (url == null) return "";
        return url.replace("'", "%27").replace("\"", "%22");
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void onSuccess(String response) {
            safeSuccess(response);
            cleanupResources();
        }

        @JavascriptInterface
        public void onFail(String message) {
            safeError("JS Error: " + message);
            cleanupResources();
        }
    }
}

