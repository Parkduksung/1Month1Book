package com.rsupport.mobile.agent.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.ui.settings.delete.AgentDeleteActivity;
import com.rsupport.mobile.agent.utils.Utility;

import java.util.ArrayList;

import com.rsupport.mobile.agent.ui.about.LicenceActivity;
import com.rsupport.mobile.agent.ui.adapter.InfoItemAdapter;
import com.rsupport.mobile.agent.ui.base.InfoListItem;
import com.rsupport.mobile.agent.ui.adapter.InfoListItemAdapter;
import com.rsupport.mobile.agent.ui.views.RVToast;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.terms.TermsActivity;
import com.rsupport.mobile.agent.ui.tutorial.TutorialSelectActivity;
import com.rsupport.mobile.agent.constant.GlobalStatic;

import org.koin.java.KoinJavaComponent;

import kotlin.Lazy;

public class SettingPageActivity extends RVCommonActivity {

    InfoItemAdapter agentAdapter1 = null;
    InfoListItemAdapter agentAdapter2 = null;

    ListView agentList1 = null;
    ListView agentList2 = null;

    /**
     * Agent detail Information ListArray
     **/
    ArrayList<InfoListItem> agentArrayList1 = null;
    ArrayList<InfoListItem> agentArrayList2 = null;

    private final static int EVENT_ID_NO = 0;

    private final static int EVENT_ID_VERSION = 10; // Version Information
    private final static int EVENT_ID_WIFI_MAC = 20; // Wi-Fi MAC Address
    private final static int EVENT_ID_LICENSE = 30; // License Information
    private final static int EVENT_ID_TUTORIAL = 40; // Tutorial
    private final static int EVENT_ID_CONFIGURATION = 50; // Configuration

    private final static int EVENT_ID_LOG_VIEW = 60; // Log View
    private final static int EVENT_ID_CALL_AGENT_DELTE_PAGE = 70; // Call Agent delete Page
    private final static int EVENT_ID_TERMS = 80; // Configuration
    private final static int EVENT_ID_PRIVACY = 90; // Configuration

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_page);
        setTitle(getResources().getString(R.string.setting), true, true);
        agentList1 = (ListView) findViewById(R.id.agentinfo_list1);
        agentList2 = (ListView) findViewById(R.id.agentinfo_list2);

        ImageButton btnTitleRight = (ImageButton) findViewById(R.id.right_button);
        btnTitleRight.setVisibility(View.INVISIBLE);

        setLeftButtonBackground(R.drawable.button_headerback);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        makeFirstListView();
        makeSecondListView();
    }

    private void makeFirstListView() {
        agentArrayList1 = new ArrayList<InfoListItem>();

        agentArrayList1.add(new InfoListItem(EVENT_ID_VERSION, getString(R.string.aboutversion), Utility.getVersionName(getApplicationContext())));
        String macAddress = GlobalStatic.getMacAddress(this);
        agentArrayList1.add(new InfoListItem(EVENT_ID_WIFI_MAC, getString(R.string.macaddress), macAddress, InfoListItem.COPY_BUTTON));
        agentArrayList1.add(new InfoListItem(EVENT_ID_LICENSE, null, getString(R.string.license), InfoListItem.RIGHT_BUTTON));
        agentArrayList1.add(new InfoListItem(EVENT_ID_TUTORIAL, null, getString(R.string.about_tutorial), InfoListItem.RIGHT_BUTTON));
        agentArrayList1.add(new InfoListItem(EVENT_ID_TERMS, null, getString(R.string.terms_of_use), InfoListItem.RIGHT_BUTTON));
        agentArrayList1.add(new InfoListItem(EVENT_ID_PRIVACY, null, getString(R.string.privacy_policy), InfoListItem.RIGHT_BUTTON));

        agentAdapter1 = new InfoItemAdapter(this, agentArrayList1, false);
        agentList1.setAdapter(agentAdapter1);

        InfoListItemAdapter.getListViewSize(agentList1);
    }

    private void makeSecondListView() {

        agentArrayList2 = new ArrayList<InfoListItem>();

        agentArrayList2.add(new InfoListItem(EVENT_ID_CONFIGURATION, null, getString(R.string.agent_setting), InfoListItem.RIGHT_BUTTON));
        agentArrayList2.add(new InfoListItem(EVENT_ID_LOG_VIEW, null, getString(R.string.agent_log), InfoListItem.RIGHT_BUTTON));
        agentArrayList2.add(new InfoListItem(EVENT_ID_CALL_AGENT_DELTE_PAGE, null, getString(R.string.agentlist_menu_agentremove), InfoListItem.RIGHT_BUTTON));

        agentAdapter2 = new InfoListItemAdapter(this, agentArrayList2);
        agentList2.setAdapter(agentAdapter2);

        InfoListItemAdapter.getListViewSize(agentList2);
    }

    private void copyClipbordMacAddr() {
        String macAddr = GlobalStatic.getMacAddress(this);
        if (Build.VERSION.SDK_INT >= 11) {
            android.content.ClipboardManager clipBoard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipBoard.setText(macAddr);
        } else {
            android.text.ClipboardManager clipBoard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipBoard.setText(macAddr);
        }

        RVToast.makeText(this, "Copy : " + macAddr, Toast.LENGTH_SHORT).show();
    }


    private void startLicenceActivity() {
        Intent intent = new Intent(this, LicenceActivity.class);
        intent.putExtra("AGENT_CALL", true);
        startActivity(intent);
        startRightSlideAnimation();
    }

    private void startTermsActivity(Boolean isTerms) {
        Intent intent = new Intent(this, TermsActivity.class);
        intent.putExtra("TERMS", isTerms);
        startActivity(intent);
        startRightSlideAnimation();
    }

    private void startTutorial() {
        short tutorialType = configRepositoryLazy.getValue().getProductType() == GlobalStatic.PRODUCT_PERSONAL ? TutorialSelectActivity.TUTORIAL_TYPE_PERSON : TutorialSelectActivity.TUTORIAL_TYPE_CORP;
        Intent intent = new Intent(this, com.rsupport.mobile.agent.ui.tutorial.TutorialActivity.class);
        intent.putExtra("about", true);
        intent.putExtra(TutorialSelectActivity.TUTORIAL_TYPE, tutorialType);
        intent.putExtra("AGENT_CALL", true);
        startActivity(intent);
        startRightSlideAnimation();
    }

    private void startDetailActivity(Context context, Class cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
        startRightSlideAnimation();
    }


    @Override
    public void eventDelivery(int event) {
        if (dialog != null) {
            dialog.dismiss();
        }
        switch (event) {
            case EVENT_ID_VERSION:
                break;
            case EVENT_ID_WIFI_MAC:
                copyClipbordMacAddr();
                break;
            case EVENT_ID_LICENSE:
                startLicenceActivity();
                break;
            case EVENT_ID_TERMS:
                startTermsActivity(true);
                break;
            case EVENT_ID_PRIVACY:
                startTermsActivity(false);
                break;
            case EVENT_ID_TUTORIAL:
                startTutorial();
                break;
            case EVENT_ID_CONFIGURATION:
                startDetailActivity(this, AgentSettingActivity.class);
                break;
            case EVENT_ID_LOG_VIEW:
                startDetailActivity(this, AgentLogActivity.class);
                break;
            case EVENT_ID_CALL_AGENT_DELTE_PAGE:
                startDetailActivity(this, AgentDeleteActivity.class);
                break;
            case EVENT_ID_NO:
                break;
        }
    }

}
