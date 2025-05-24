package com.two.channelmyanmar.bottomsheet;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.two.channelmyanmar.R;
import com.two.channelmyanmar.api.ExtractorListener;
import com.two.channelmyanmar.databinding.LoadingSheetBinding;
import com.two.channelmyanmar.databinding.LoadingSheetHideBinding;
import com.two.channelmyanmar.preference.PreferenceHelper;
import com.two.my_libs.views.popup.ArrowDirection;
import com.two.my_libs.views.popup.BubbleLayout;
import com.two.my_libs.views.popup.BubblePopupHelper;
import com.two.my_libs.views.taptarget.TapTarget;
import com.two.my_libs.views.taptarget.TapTargetView;

public class LoadingSheet extends BottomSheetDialogFragment {
    private final String TAG = this.getClass().getSimpleName();
    private LoadingSheetBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String baseUrl;
    private boolean found = false;
    private WebView webView;
    private int sec = 0;

    private static final int LINK_FIND_POLL_INTERVAL = 2000; // Polling interval to find link
    private static final int LINK_FIND_TIMEOUT_SEC = 20; // Timeout after which consider link not found
    private int currentProgress = 0;
    private boolean injected = false;

    private ExtractorListener listener;
    private boolean isDirect;
    private PreferenceHelper helper;

    public LoadingSheet(String url, ExtractorListener listener,boolean isDirect) {
        this.baseUrl = url;
        this.listener = listener;
        this.isDirect = isDirect;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        View view = (View) getView().getParent();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //
        Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_inject);

//        TapTargetView.showFor(requireActivity(),                 // `this` is an Activity
//                TapTarget.forView(binding.fab, "This is a target", "We have the best targets, believe me")
//                        // All options below are optional
//                        .outerCircleColor(com.two.my_libs.R.color.red)      // Specify a color for the outer circle
//                        .outerCircleAlpha(0.96f)            // Specify the alpha amount for the outer circle
//                        .targetCircleColor(R.color.white)   // Specify a color for the target circle
//                        .titleTextSize(20)                  // Specify the size (in sp) of the title text
//                        .titleTextColor(R.color.white)      // Specify the color of the title text
//                        .descriptionTextSize(10)            // Specify the size (in sp) of the description text
//                        .descriptionTextColor(com.two.my_libs.R.color.red)  // Specify the color of the description text
//                        .textColor(com.two.my_libs.R.color.blue_color)            // Specify a color for both the title and description text
//                        .textTypeface(Typeface.SANS_SERIF)  // Specify a typeface for the text
//                        .dimColor(R.color.black)            // If set, will dim behind the view with 30% opacity of the given color
//                        .drawShadow(true)                   // Whether to draw a drop shadow or not
//                        .cancelable(false)                  // Whether tapping outside the outer circle dismisses the view
//                        .tintTarget(true)                   // Whether to tint the target view's color
//                        .transparentTarget(false)           // Specify whether the target is transparent (displays the content underneath)
//                        .icon(icon)                    // Specify a custom drawable to draw as the target
//                        .targetRadius(60),                  // Specify the target radius (in dp)
//                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
//                    @Override
//                    public void onTargetClick(TapTargetView view) {
//                        super.onTargetClick(view);      // This call is optional
//                        //doSomething();
//                    }
//                });
        binding.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"Click",Toast.LENGTH_SHORT).show();
            }
        });
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDirect) {
                    String jsCode = "javascript:(function() { var b=document.getElementById('btndownload');b.click();})();";
                    webView.evaluateJavascript(jsCode, null);
                }else {
                    String jsCode = "javascript:(function() { var form = document.getElementById('myform');" +
                            "      if (form) { do_form($('#myform').serialize());} else { console.error('Form not found.'); };})();";
                    webView.evaluateJavascript(jsCode, null);
                    handler.postDelayed(() -> startLinkFindPolling(webView), 500);
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        helper = new PreferenceHelper(getContext());
        binding = LoadingSheetBinding.inflate(inflater, container, false);
        webView = binding.loadingwebview;
        if (helper.showInject()){
//            binding.loadingwebview.setLayoutParams(new ViewGroup.LayoutParams());
        }else {
            ViewGroup.LayoutParams params = binding.loadingwebview.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            float scale = getResources().getDisplayMetrics().density;
            params.height = (int) (1*scale+0.5f);
            binding.loadingwebview.setLayoutParams(params);
        }
        setupWebView();
        if(isDirect)
            webView.loadUrl(baseUrl);
        else
            webView.loadUrl("https://noads.leetdev.net");
        return binding.getRoot();
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setInitialScale(1);
        // Enable windows opening for captcha popups
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // Use a more desktop-like user agent to bypass potential automation detection
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        //setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.105 Safari/537.36");

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (listener != null) {
                    listener.onSuccessYoteShin(url, true);
                }
                dismissAllowingStateLoss();
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, url);
                return false;
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.d(TAG, url);
                if (currentProgress == 100 && url.contains("https://www.google.com/recaptcha") && !injected) {
                    injected = true;
                    // If needed, you can inject custom JavaScript here to interact with reCAPTCHA elements.
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "onPageFinished: " + url);
                if (url.contains("https://drive.")) {
                    // Google Drive specific JavaScript execution
                    view.evaluateJavascript(
                            "var id = document.getElementById('uc-download-link');" +
                                    "if (id) { id.click(); } else { " +
                                    "   var link = window.location.href;" +
                                    "   Android.onFail('Too many users have viewed or downloaded this file recently. Please try again later.', link);" +
                                    "}", null);
                }else if (url.contains("megaup")){
                    binding.loadingtext.setText("Please Wait..");

                    injectTimerClickScript();
                }
                else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.loadingtext.setText("Extracting link... " + reverCount(sec));
                    // Delay injection slightly to ensure any captcha widget is rendered
                    handler.postDelayed(() -> injectLinkAndSubmitForm(view), 1500);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (!isAdded())
                    return;
                Log.d("Noads", "Loading Progress: " + newProgress + "%");
                currentProgress = newProgress;
                binding.loadingtext.setText("Loading link ..." + newProgress + "%");
                binding.progressBar.setProgress(newProgress);
                if (isDirect&&view.getUrl().contains("download.megaup")&&newProgress ==100){
                    binding.fab.setVisibility(View.VISIBLE);

                }else if (newProgress==100&&view.getUrl().contains("leetdev.net")){
                    String jsCode = "javascript:(function() { var inputField = document.querySelector('input[name=\"link\"]');" +
                            "if (inputField) { inputField.value = '" + baseUrl + "'; }})();";
                    view.evaluateJavascript(jsCode,null);
                    binding.fab.setVisibility(View.VISIBLE);

                }

            }
        });
    }


    public String reverCount(int current) {
        return String.valueOf(LINK_FIND_TIMEOUT_SEC - current);
    }

    public void updateUi(int sec) {
        if (!isAdded())
            return;
        handler.post(() -> binding.loadingtext.setText("Extracting... " + reverCount(sec)));
    }
    private void injectTimerClickScript() {
        // JavaScript code to check every 1 second for TimerDiv and click the anchor if it exists.
        String jsCode = "javascript:(function() {" +
                "    setInterval(function() {" +

                "        var timerDiv = document.querySelector('div.download-timer');" +
                "        if (timerDiv) {" +
                "            var aExist = timerDiv.querySelector('a');" +
                "            if (aExist) {" +
                "                aExist.click();" +
                "            }" +
                "        }" +
                "    }, 1000);" +
                "})();";

        webView.evaluateJavascript(jsCode,null);
    }

    private void startLinkFindPolling(final WebView wv) {
        final Runnable pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded())
                    return;
                if (!found) {
                    findLink(wv);
                    if (sec < LINK_FIND_TIMEOUT_SEC) { // Check against timeout constant
                        handler.postDelayed(this, LINK_FIND_POLL_INTERVAL);
                        sec += (LINK_FIND_POLL_INTERVAL / 1000); // increment seconds based on poll interval in seconds
                        updateUi(sec);
                    } else {
                        if (!found) {
                            if (listener != null) {
                                listener.onFailed("Timeout: Unable to extract link after " + LINK_FIND_TIMEOUT_SEC + " seconds.");
                            }
                            dismissAllowingStateLoss();
                        }
                    }
                }
            }
        };
        handler.postDelayed(pollingRunnable, LINK_FIND_POLL_INTERVAL);
    }

    private void injectLinkAndSubmitForm(WebView wv) {
        if (!isAdded())
            return;
        String jsCode = "var inputField = document.querySelector('input[name=\"link\"]');" +
                "if (inputField) { inputField.value = '" + baseUrl + "'; }" +
                "var form = document.getElementById('myform');" +
                "if (form) { " +
                "   do_form($('#myform').serialize());" +
                "} else { console.error('Form not found.'); }";
        wv.evaluateJavascript(jsCode, null);
        handler.postDelayed(() -> startLinkFindPolling(wv), 500);
        Log.d("Noads", "JavaScript for link injection and form submission executed.");
    }

    public void findLink(WebView wv) {
        if (!isAdded())
            return;
        handler.post(() -> {
            Log.d("Noads", "Executing JavaScript to find link...");
            wv.evaluateJavascript(
                    "var resultDiv = document.getElementById('result');" +
                            "if (resultDiv && window.getComputedStyle(resultDiv).getPropertyValue('display') !== 'none') {" +
                            "  resultDiv.getElementsByTagName('a')[0].getAttribute('href');" +
                            "} else {" +
                            "  var element = document.querySelector('.swal-title');" +
                            "  var text = document.querySelector('.swal-text');" +
                            "  if(element && element.innerText.indexOf('Oops') !== -1) {" +
                            "    'Error: Failed! ' + (text ? text.innerText : 'Captcha required');" +
                            "  } else {" +
                            "    'null';" +
                            "  }" +
                            "}",
                    value -> {
                        String result = String.valueOf(value).replace("\"", "");
                        if (result.contains("Error")) {
                            String errorMessage = result.substring("Error: ".length());
                            Log.d("Noads", "Link extraction error: " + errorMessage);
                            // If error message indicates a captcha, consider clearing cookies/cache or prompting manual action
                            if (errorMessage.toLowerCase().contains("captcha")) {
                                // Optionally clear cookies or cache here before reloading
                                webView.reload();
                            } else {
                                if (listener != null)
                                    listener.onFailed(errorMessage);
                                dismissAllowingStateLoss();
                            }
                        } else if (!"null".equals(result)) {
                            Log.d("Noads", "Final link found: " + result);
                            if (result.contains("drive")) {
                                webView.loadUrl(result);
                            } else {
                                found = true;
                                if (listener != null) {
                                    listener.onSuccessMegaUp(result);
                                }
                                dismissAllowingStateLoss();
                            }
                        } else {
                            Log.d("Noads", "Result is null, link not found yet.");
                        }
                    });
        });
    }

    // --- Lifecycle and Cleanup ---
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null); // Clean up handler posts
        if (webView != null) {
            // Important: Stop loading, remove from view, and THEN destroy
            webView.stopLoading();
            ((ViewGroup) webView.getParent()).removeView(webView); // Remove from layout first
            webView.removeAllViews(); // Remove child views if any
            webView.destroy(); // Destroy the internal state
            webView = null;
        }
        binding = null; // Release binding
        Log.d(TAG, "onDestroyView: WebView and Handler cleaned up.");
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "BottomSheet dismissed.");
        // Ensure listener is nullified if not already done
        listener = null;
        // Optionally ensure handler messages are stopped again
        handler.removeCallbacksAndMessages(null);
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onFail(String response, String link) {
            if (!isAdded())
                return;
            if (listener != null) {
                listener.onSuccessYoteShin(link, false);
            }
            dismissAllowingStateLoss();
        }

        @JavascriptInterface
        public void onFail(String response) {
            if (!isAdded())
                return;
            Log.e("Noads", "JavaScript onFail callback received: " + response);
        }
    }
}

