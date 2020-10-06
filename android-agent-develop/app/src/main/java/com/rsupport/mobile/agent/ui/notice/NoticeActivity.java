package com.rsupport.mobile.agent.ui.notice;

import java.util.TimeZone;

import kotlin.Lazy;

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
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.ui.launcher.LauncherActivity;

import org.koin.java.KoinJavaComponent;


public class NoticeActivity extends RVCommonActivity {

    //	private TutorialGallery gallery;
    private boolean isAboutPage;

    private WebView noticeContent;
    private ProgressBar webProgress;
    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle order = getIntent().getExtras();
        if (order != null) {
            isAboutPage = order.getBoolean("about", false);
        }

        setContentView(R.layout.notice, R.layout.layout_common_bg_ns_no_margin);

        setTitle(R.string.about_notice, true, false);

        setLeftButtonBackground(R.drawable.button_headerback);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isAboutPage) {
                    startMainActivity();
                } else {
                    startAboutActivity();
                }
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

        String locale = getSystemLanguage(this);
        noticeContent = (WebView) findViewById(R.id.notice_content);
        noticeContent.getSettings().setJavaScriptEnabled(true);
        noticeContent.loadUrl(getString(R.string.aboutweblink) + getString(R.string.url_notice) + "?ctype=3&language=" + locale + "&Timezoneoffset=" + getTimeZoneOffset());

        final Activity activity = this;
        noticeContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webProgress.setProgress(newProgress);
            }
        });

        noticeContent.setWebViewClient(new WebViewClient() {

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

    private void startMainActivity() {
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startAboutActivity() {
//		Intent intent = new Intent(this, AboutActivity.class);
//		intent.putExtra(PreferenceConstant.RV_BUNDLE_ACTIVITY_BACK, PreferenceConstant.RV_TRUE);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        configRepositoryLazy.getValue().syncNoticeSeq();
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
