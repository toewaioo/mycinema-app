package com.two.channelmyanmar.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.two.channelmyanmar.R;
import com.two.channelmyanmar.api.ExtractorListener;
import com.two.channelmyanmar.api.Noads;
import com.two.channelmyanmar.bottomsheet.LoadingSheet;
import com.two.channelmyanmar.databinding.BrowserActivityBinding;
import com.two.channelmyanmar.fragment.detail.MovieChildFragment;
import com.two.my_libs.ActivityTheme;
import com.two.my_libs.base.BaseThemeActivity;

public class BrowserActivity extends BaseThemeActivity {
    private BrowserActivityBinding browserActivity;
    Handler handler= new Handler(Looper.getMainLooper());
    String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        browserActivity = BrowserActivityBinding.inflate(getLayoutInflater());
        setContentView(browserActivity.getRoot());
        String url = getIntent().getStringExtra("url");

        browserActivity.browserWebview.getSettings().setJavaScriptEnabled(true);
        browserActivity.browserWebview.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

        browserActivity.browserWebview.addJavascriptInterface(new WebAppInterface(), "Android");
        browserActivity.browserWebview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Toast.makeText(getApplicationContext(),url,Toast.LENGTH_SHORT).show();
            }
        });

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                baseUrl = extras.getString("baseUrl");
                if (baseUrl != null) {
                    browserActivity.browserWebview.loadUrl(baseUrl);
                }
            }
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onSuccess(String response) {
            browserActivity.browserWebview.evaluateJavascript( "javascript:(function() { var b=document.getElementById('btndownload');b.click();})();",null);

            // Called from Javascript when link is successfully extracted within the web page itself.
            // This is another pathway for success, in addition to polling mechanism.
           // cb.onSuccessMegaUp(response);

            Log.d("Noads", "JavaScript onSuccess callback received: " + response);
            //destroyWebViewOnUiThread("");
        }

        @JavascriptInterface
        public void onFail(String response, String link) { // Pass back the link for potential error context
            // cb.onError("Link extraction failed from webpage: " + response + ". URL attempted: " + link);
           // cb.onSuccessYoteShin(link,false);
            Log.e("Noads", "JavaScript onFail callback received: " + response + ", URL: " + link);
            //destroyWebViewOnUiThread("Link extraction failed from webpage: " + response + ". URL attempted: " + link);
        }

        @JavascriptInterface
        public void onFail(String response) { // Overloaded method in case link is not easily available in JS context
            //cb.onError("Link extraction failed from webpage: " + response );
            Log.e("Noads", "JavaScript onFail callback received: " + response);
            //destroyWebViewOnUiThread("Link extraction failed from webpage: " + response);
        }
    }

    @Override
    protected void configTheme(ActivityTheme activityTheme) {
        activityTheme.setThemes(new int[]{R.style.AppTheme_Light, R.style.AppTheme_Black});
        activityTheme.setStatusBarColorAttrRes(R.attr.colorPrimary);
        activityTheme.setSupportMenuItemThemeEnable(true);
    }
}
