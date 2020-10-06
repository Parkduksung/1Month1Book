package com.rsupport.mobile.agent.ui.terms;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rsupport.mobile.agent.R;

import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.views.RVToast;

/**
 * Created by hyun on 2017. 10. 23..
 */

public class TermsActivity extends RVCommonActivity {

    private WebView termsContent;
    private ProgressBar webProgress;
    private boolean isTerms;
    private final String SERVER_ADDR = "https://content.rview.com/";
    private final String TERMS_POST_ADDR = "/terms-and-conditions/";
    private final String PRIVACY_PSOT_ADDR = "/privacy-policy/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice, R.layout.layout_common_bg_ns_no_margin);

        Intent intent = getIntent();
        isTerms = intent.getBooleanExtra("TERMS", true);
        if (isTerms) {
            setTitle(R.string.terms_of_use, true, false);
        } else {
            setTitle(R.string.privacy_policy, true, false);
        }
        setLeftButtonBackground(R.drawable.button_headerback);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startLeftSlideAnimation();
                finish();
            }
        });

        webProgress = (ProgressBar) findViewById(R.id.web_progress);

        termsContent = (WebView) findViewById(R.id.notice_content);
        termsContent.getSettings().setJavaScriptEnabled(true);
        termsContent.loadUrl(getLoadURL());

        final Activity activity = this;
        termsContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webProgress.setProgress(newProgress);
            }
        });

        termsContent.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webProgress.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                RVToast.makeText(activity, R.string.cmderr_system_reboot_fail, Toast.LENGTH_LONG).show();
            }
        });

    }

    public String getSystemLanguage() {
        String ret = "ko-KR";
        //    	String country = getResources().getConfiguration().locale.getCountry();
        String locale = getResources().getConfiguration().locale.toString();

        if (locale.contains("ko")) {
            ret = "ko";
        } else if (locale.contains("en")) {
            ret = "en";
        } else if (locale.contains("ja")) {
            ret = "ja";
        } else if (locale.equals("zh") || locale.contains("zh_CN")) {
            ret = "zh-cn";
        } else if (locale.contains("zh_TW")) {
            ret = "zh-tw";
        } else {
            ret = "en-int";
        }

        return ret;
    }

    private String getLoadURL() {
        if (isTerms) {
            return SERVER_ADDR + getSystemLanguage() + TERMS_POST_ADDR;
        } else {
            return SERVER_ADDR + getSystemLanguage() + PRIVACY_PSOT_ADDR;
        }
    }
}
