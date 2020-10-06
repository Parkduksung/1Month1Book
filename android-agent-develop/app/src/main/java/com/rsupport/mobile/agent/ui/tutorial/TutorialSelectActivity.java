/********************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2013 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.
 *
 * FileName: TutorialSelectActivity.java
 * Author  : "kyeom@rsupport.com"
 * Date    : 2013. 1. 3.
 * Purpose : 튜토리얼 선택(개인용 or 기업용) 화면
 *
 * [History]
 *
 * 2013. 1. 3. -Initialize
 *
 */
package com.rsupport.mobile.agent.ui.tutorial;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;

import org.koin.java.KoinJavaComponent;

import kotlin.Lazy;

import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.launcher.LauncherActivity;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.constant.PreferenceConstant;

/**
 * @author "kyeom@rsupport.com"
 */
public class TutorialSelectActivity extends RVCommonActivity {

    public static final String TUTORIAL_TYPE = "tutorial_type";
    public static final short TUTORIAL_TYPE_PERSON = 0;
    public static final short TUTORIAL_TYPE_CORP = 1;
    public static final short TUTORIAL_TYPE_CHINA_PERSON = 2;
    public static final short TUTORIAL_TYPE_CHINA_CORP = 3;

    private final String BUTTON_CHECKED = "1";
    private final String BUTTON_UNCHECKED = "0";


    private Button personBtn;
    private Button corpBtn;

    private RelativeLayout personLayout;
    private RelativeLayout corpLayout;

    private ImageView tutorialSelectImg;

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initApp();

        //Tutorial occured check
        if (configRepositoryLazy.getValue().isShowTutorial()) {
            startMainActivity(this);
            finish();
            return;
        }
        //China check
        if (GlobalStatic.isChinaPackage(this)) {
            savePreferences();
            startTutorial();
            finish();
            return;
        }

        setContentView(R.layout.tutorial_select, R.layout.layout_common_bg_ns_no_margin);

        hideTitle();

        tutorialSelectImg = (ImageView) findViewById(R.id.tutorial_select_img);

        personBtn = (Button) findViewById(R.id.tutorial_person_btn);
        personBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                changeBtnImg(personBtn);
                configRepositoryLazy.getValue().setProductType(GlobalStatic.PRODUCT_PERSONAL);
            }
        });
        personLayout = (RelativeLayout) findViewById(R.id.tutorial_person_layout);
        personLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        personBtn.setPressed(true);
                        v.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        changeBtnImg(personBtn);
                        v.setSelected(true);
                        corpLayout.setSelected(false);
                        configRepositoryLazy.getValue().setProductType(GlobalStatic.PRODUCT_PERSONAL);
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        personBtn.setPressed(false);
                        v.setPressed(false);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }

                return true;
            }
        });

        personLayout.setSelected(true);

        corpBtn = (Button) findViewById(R.id.tutorial_corp_btn);
        corpBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                changeBtnImg(corpBtn);
                configRepositoryLazy.getValue().setProductType(GlobalStatic.PRODUCT_CORP);
            }
        });
        corpLayout = (RelativeLayout) findViewById(R.id.tutorial_corp_layout);
        corpLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        corpBtn.setPressed(true);
                        v.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        changeBtnImg(corpBtn);
                        v.setSelected(true);
                        personLayout.setSelected(false);
                        configRepositoryLazy.getValue().setProductType(GlobalStatic.PRODUCT_CORP);
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_OUTSIDE:
                        corpBtn.setPressed(false);
                        v.setPressed(false);
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }

                return true;
            }
        });
        findViewById(R.id.tutorial_next_btn).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    savePreferences();
                    startTutorial();
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        if (configRepositoryLazy.getValue().getProductType() == GlobalStatic.PRODUCT_CORP) {
            changeBtnImg(corpBtn);
            corpLayout.setSelected(true);
        } else {
            changeBtnImg(personBtn);
            personLayout.setSelected(true);
        }
        super.onResume();
    }

    private void changeBtnImg(Button eventBtn) {

        if (eventBtn.equals(personBtn)) {
            personBtn.setBackgroundResource(R.drawable.button_radio_on);
            personBtn.setContentDescription(BUTTON_CHECKED);
            corpBtn.setBackgroundResource(R.drawable.button_radio_off);
            corpBtn.setContentDescription(BUTTON_UNCHECKED);

            tutorialSelectImg.setBackgroundResource(R.drawable.img_joinus01);
        } else {
            personBtn.setBackgroundResource(R.drawable.button_radio_off);
            personBtn.setContentDescription(BUTTON_UNCHECKED);
            corpBtn.setBackgroundResource(R.drawable.button_radio_on);
            corpBtn.setContentDescription(BUTTON_CHECKED);

            tutorialSelectImg.setBackgroundResource(R.drawable.img_joinus02);
        }
    }

    private void changeLayoutImg(View eventView, MotionEvent event) {
//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			eventView.setBackgroundResource(R.drawable.bg_tutorial_select_press);
//		} else if (event.getAction() == MotionEvent.ACTION_UP) {
//			eventView.setBackgroundResource(R.drawable.bg_tutorial_select);
//		}
    }

    private void startTutorial() {
        Intent intent = new Intent(this, com.rsupport.mobile.agent.ui.tutorial.TutorialActivity.class);

        if (GlobalStatic.isChinaPackage(this)) {
            intent.putExtra(TUTORIAL_TYPE, TUTORIAL_TYPE_CHINA_PERSON);
        } else {
            intent.putExtra(TUTORIAL_TYPE, personBtn.getContentDescription().equals(BUTTON_CHECKED) ? TUTORIAL_TYPE_PERSON : TUTORIAL_TYPE_CORP);
        }
        startActivity(intent);
        finish();
    }

    private void startMainActivity(Context context) {
        Intent intent = new Intent(context, LauncherActivity.class);
        startActivity(intent);
    }

    private void savePreferences() {
        configRepositoryLazy.getValue().setShowTutorial(true);

        Editor editor = getSharedPreferences(PreferenceConstant.RV_PREF_SETTING_INIT, Context.MODE_PRIVATE).edit();

        if (personBtn == null) {
            editor.putInt(PreferenceConstant.RV_SERVER_TYPE, GlobalStatic.PRODUCT_PERSONAL);
        } else {
            editor.putInt(PreferenceConstant.RV_SERVER_TYPE, personBtn.getContentDescription().equals(BUTTON_CHECKED) ? GlobalStatic.PRODUCT_PERSONAL : GlobalStatic.PRODUCT_CORP);
        }
        editor.commit();
    }

    private void initApp() {

        GlobalStatic.loadSettingURLInfo(TutorialSelectActivity.this);
        GlobalStatic.loadResource(TutorialSelectActivity.this);

        new Thread() {
            public void run() {
                GlobalStatic.loadAppInfo(TutorialSelectActivity.this);
            }
        }.start();
    }

    private boolean isNoTutorial() {
        if (GlobalStatic.isServerPackage(this) ||
                GlobalStatic.isAlcatelPackage(this)) {
            return true;
        }
        return false;
    }

}
