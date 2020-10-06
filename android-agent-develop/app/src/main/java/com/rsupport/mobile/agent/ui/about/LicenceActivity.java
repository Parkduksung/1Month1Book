package com.rsupport.mobile.agent.ui.about;

import java.util.TimeZone;

import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.views.RVToast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rsupport.mobile.agent.R;


public class LicenceActivity extends RVCommonActivity {

    //	private TutorialGallery gallery;
    private boolean isAboutPage;

    private WebView licenceContent;
    private ProgressBar webProgress;

    private boolean isAgentCall;

    private int pageNumber = 0;
    private final static int PAGE_MAIN = 0;
    private final static int PAGE_TERMS_OF_USE = 1;
    private final static int PAGE_PRIVACY_POLICY = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        isAgentCall = intent.hasExtra("AGENT_CALL");

        setContentView(R.layout.member_join, R.layout.layout_common_bg_ns_no_margin);

        setTitle(R.string.license, true, false);

        setLeftButtonBackground(R.drawable.button_headerback);

        webProgress = (ProgressBar) findViewById(R.id.web_progress);

        String locale = getSystemLanguage(this);
        licenceContent = (WebView) findViewById(R.id.notice_content);
        licenceContent.getSettings().setJavaScriptEnabled(true);

        licenceContent.loadUrl(getString(R.string.aboutweblink) + getString(R.string.url_licence));

        final Activity activity = this;
        licenceContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webProgress.setProgress(newProgress);
            }
        });

        licenceContent.setWebViewClient(new WebViewClient() {

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
                if (!isAgentCall)
                    v.requestFocusFromTouch();
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public String getSystemLanguage(Context context) {
        String ret = "ko-KR";
//    	String country = getResources().getConfiguration().locale.getCountry();
        String locale = context.getResources().getConfiguration().locale.toString();

        if (locale.contains("ko")) {
            ret = "ko-KR";
        } else if (locale.contains("en")) {
            ret = "en-US";
        } else if (locale.contains("ja")) {
            ret = "ja-JP";
        } else if (locale.equals("zh") || locale.contains("zh_CN")) {
            ret = "zh-CN";
        } else if (locale.contains("zh_TW")) {
            ret = "zh-TW";
        }

        return ret;
    }

    private String getTimeZoneOffset() {

        int offGMTMinute = TimeZone.getDefault().getRawOffset() / (60 * 1000);
        String offset = String.valueOf(offGMTMinute);
        return offset;
    }
}
