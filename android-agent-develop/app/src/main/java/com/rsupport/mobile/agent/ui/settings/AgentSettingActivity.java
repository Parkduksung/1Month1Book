package com.rsupport.mobile.agent.ui.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.repo.config.ProxyInfo;
import com.rsupport.mobile.agent.ui.settings.device.AgentDeviceNameSettingActivity;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;

import com.rsupport.mobile.agent.api.model.GroupInfo;

import kotlin.Lazy;

import com.rsupport.mobile.agent.ui.adapter.AboutItemAdapter;
import com.rsupport.mobile.agent.utils.AgentCommon;
import com.rsupport.mobile.agent.ui.base.InfoListItem;
import com.rsupport.mobile.agent.ui.adapter.InfoListItemAdapter;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.constant.PreferenceConstant;
import com.rsupport.util.log.RLog;

@SuppressLint("NewApi")
public class AgentSettingActivity extends RVCommonActivity {

    private final static int EVENT_ID_NO = 0;
    private final static int EVENT_ID_DISPLAY_NAME = 10; // 표시명 설정
    private final static int EVENT_ID_PROXY = 60; // 프록시 사용
    private final static int EVENT_ID_ALLOW_IP = 70; // 허용 IP 설정
    private final static int EVENT_ID_ALLOW_MAC = 80; // 허용 MAC 설정
    private final static int EVENT_ID_ACCES_ACCOUNT = 90; // 허용 MAC 설정

    public static final int SUB_ID_PROXY_ADDR = 61;
    public static final int SUB_ID_PROXY_PORT = 62;
    public static final int SUB_ID_PROXY_ID = 63;
    public static final int SUB_ID_PROXY_PW = 64;

    /**
     * AboutSetting Activity Information ListBox
     **/
    AboutItemAdapter aboutAdapter1 = null;

    /**
     * AboutSetting Activity Information ListBox View
     **/
    ListView aboutList1 = null;

    /**
     * AboutSetting Activity Information ListArray
     **/
    ArrayList<InfoListItem> aboutArrayList1 = null;

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.agent_setting_activity, R.layout.layout_common_bg_margin);
        setTitle(R.string.agent_setting, true, false);
        setLeftButtonBackground(R.drawable.button_headerback);
        findViewById(R.id.copyright).setVisibility(View.GONE);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backActivity();
            }
        });

        aboutList1 = (ListView) findViewById(R.id.about_list1);

        loadSettingInfo();
        makeFirstListView();
    }

    @Override
    /** Save All information **/
    public void onBackPressed() {
        backActivity();
        super.onBackPressed();
    }

    private void backActivity() {
        if (configRepositoryLazy.getValue().getStartApp()) {
            // login state > go to AgentListActivity
            Intent intent = new Intent(this, SettingPageActivity.class);
            GroupInfo group = AgentCommon.getParentInfo(GlobalStatic.g_agentInfo.groupid);
            intent.putExtra("PARENTNAME", AgentCommon.getMostParentName());
            intent.putExtra("PARENTID", AgentCommon.getMostParentID());
            intent.putExtra("REFRESH", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            startLeftSlideAnimation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setSettingInfoAll();
    }

    private void makeFirstListView() {
        RLog.d("makeFirstListView");

        aboutArrayList1 = new ArrayList<InfoListItem>();
        aboutArrayList1.add(new InfoListItem(EVENT_ID_DISPLAY_NAME, null, getString(R.string.agent_display_name), InfoListItem.RIGHT_BUTTON));
        aboutArrayList1.add(new InfoListItem(EVENT_ID_ACCES_ACCOUNT, null, getString(R.string.agent_access_account), InfoListItem.RIGHT_BUTTON));
        InfoListItem proxyItem = new InfoListItem(EVENT_ID_PROXY, null, getString(R.string.proxyuse), InfoListItem.TOGGLE_BUTTON_OFF);
        if (configRepositoryLazy.getValue().isProxyUse()) {
            proxyItem.isToggleOn = true;
            proxyItem.isToggleOff = false;
            proxyItem.isDivider = false;
            aboutArrayList1.add(proxyItem);

            ProxyInfo proxyInfo = configRepositoryLazy.getValue().getProxyInfo();

            InfoListItem proxyAddr = new InfoListItem(EVENT_ID_NO, getString(R.string.proxyaddr), proxyInfo.getAddress(), InfoListItem.EDIT_BOX);
            proxyAddr.setChildID(SUB_ID_PROXY_ADDR);
            proxyAddr.isDivider = false;
            aboutArrayList1.add(proxyAddr);

            InfoListItem proxyPort = new InfoListItem(EVENT_ID_NO, getString(R.string.proxyport), proxyInfo.getPort(), InfoListItem.EDIT_BOX);
            proxyPort.setChildID(SUB_ID_PROXY_PORT);
            proxyPort.isDivider = false;
            aboutArrayList1.add(proxyPort);

            InfoListItem proxyId = new InfoListItem(EVENT_ID_NO, getString(R.string.proxyid), proxyInfo.getId(), InfoListItem.EDIT_BOX);
            proxyId.setChildID(SUB_ID_PROXY_ID);
            proxyId.isDivider = false;
            aboutArrayList1.add(proxyId);

            InfoListItem proxyPw = new InfoListItem(EVENT_ID_NO, getString(R.string.proxypassword), proxyInfo.getPwd(), InfoListItem.EDIT_BOX);
            proxyPw.setChildID(SUB_ID_PROXY_PW);
            aboutArrayList1.add(proxyPw);
        } else {
            aboutArrayList1.add(proxyItem);
        }

        aboutArrayList1.add(new InfoListItem(EVENT_ID_ALLOW_IP, null, getString(R.string.agent_allow_ip), InfoListItem.RIGHT_BUTTON));
        aboutArrayList1.add(new InfoListItem(EVENT_ID_ALLOW_MAC, null, getString(R.string.agent_allow_mac), InfoListItem.RIGHT_BUTTON));

        aboutAdapter1 = new AboutItemAdapter(this, aboutArrayList1);
        aboutList1.setAdapter(aboutAdapter1);

        InfoListItemAdapter.getListViewSize(aboutList1);
    }

    private void startDetailActivity(Context context, Class cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
        startRightSlideAnimation();
    }

    private void loadSettingInfo() {
        GlobalStatic.loadSettingURLInfo(this);
    }

    /**
     * SharedPreferences item save
     * param - SharedpreferencesName(key(String), value(T)
     **/
    public <T> void setSettingInfo(String key, T value) {
        SharedPreferences pref = getSharedPreferences(PreferenceConstant.RV_PREF_SETTING_INIT, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        editor.commit();
    }

    /**
     * All Setting items save
     **/
    private void setSettingInfoAll() {
        Global.getInstance().getWebConnection().setNetworkInfo();
    }

    @Override
    public void eventDelivery(int event) {

        switch (event) {
            case EVENT_ID_NO:
                if (dialog != null) {
                    dialog.dismiss();
                }
                break;
            case EVENT_ID_DISPLAY_NAME:
                startActivity(new Intent(this, AgentDeviceNameSettingActivity.class));
                break;
            case EVENT_ID_ACCES_ACCOUNT:
                startActivity(new Intent(this, AgentAccessAcountChangeActivity.class));
                break;

            case EVENT_ID_PROXY:
                // if "on" state than open child box
                configRepositoryLazy.getValue().toggleProxyUse();
                setSettingInfoAll();
                makeFirstListView();
                break;
            case EVENT_ID_ALLOW_IP:
                startDetailActivity(this, AgentAllowIPSettingActivity.class);
                break;
            case EVENT_ID_ALLOW_MAC:
                startDetailActivity(this, AgentAllowMACSettingActivity.class);
                break;
        }
    }
}
