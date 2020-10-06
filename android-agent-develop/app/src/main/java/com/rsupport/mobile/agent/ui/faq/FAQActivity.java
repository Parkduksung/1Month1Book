package com.rsupport.mobile.agent.ui.faq;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.rsupport.mobile.agent.R;

import com.rsupport.mobile.agent.ui.base.RVCommonActivity;


public class FAQActivity extends RVCommonActivity {

//	private TutorialGallery gallery;

    private WebView faqWebView;
    private ProgressBar webProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice, R.layout.layout_common_bg_ns_no_margin);

        setTitle(R.string.string_faq, true, false);

        setLeftButtonBackground(R.drawable.button_headerback);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startLeftSlideAnimation();
                finish();
            }
        });
        btnTitleLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        webProgress = (ProgressBar) findViewById(R.id.web_progress);

        faqWebView = (WebView) findViewById(R.id.notice_content);
        faqWebView.getSettings().setJavaScriptEnabled(true);
        faqWebView.loadUrl(getResources().getString(R.string.faq_website));

        faqWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webProgress.setProgress(newProgress);
            }
        });

        faqWebView.setWebViewClient(new WebViewClient() {

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
            }
        });

    }


}
