package com.rsupport.mobile.agent.ui.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.rsupport.mobile.agent.R;

import java.util.ArrayList;

import com.rsupport.mobile.agent.api.model.GroupInfo;
import com.rsupport.mobile.agent.utils.AgentCommon;
import com.rsupport.mobile.agent.ui.adapter.InfoItemAdapter;
import com.rsupport.mobile.agent.ui.base.InfoListItem;
import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.utils.AgentSQLiteHelper;
import com.rsupport.mobile.agent.constant.GlobalStatic;

@SuppressLint("NewApi")
public class AgentLogActivity extends RVCommonActivity {

    InfoItemAdapter InfoItemAdapter = null;

    ListView logList = null;

    ArrayList<InfoListItem> logListArrayList = new ArrayList<InfoListItem>();
    AgentSQLiteHelper sqlHelper = null;

    final int EVENTID_LOG_DELETE = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.agent_log_activity, R.layout.layout_common_bg_ns_no_margin);

        setTitle(R.string.agent_log_title, true, false);
        setLeftButtonBackground(R.drawable.button_headerback);

        setBottomTitle(R.string.agent_log_remove, null);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                backActivity();
            }
        });
        logList = (ListView) findViewById(R.id.about_list1);

        sqlHelper = new AgentSQLiteHelper(this);
        sqlHelper.initDB();

    }

    /**
     * 하단 타이틀바 (클릭버튼용) 기능
     **/
    public void Click_ButtomTitle(View v) {
        // 로그 제거 로직 추가필요

        showAlertDialog(null, getString(R.string.log_data_dalete_confirm_message), RVDialog.STYLE_NOTICE, R.string.common_ok, EVENTID_LOG_DELETE, true);
    }

    @Override
    public void eventDelivery(int event) {
        switch (event) {
            case EVENTID_LOG_DELETE:
                if (dialog != null) {
                    dialog.dismiss();
                }
                sqlHelper.deleteAllData(AgentSQLiteHelper.LOG_TABLE);
                makeLogItemLists();
                break;
        }
        super.eventDelivery(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeLogItemLists();
    }

    @Override
    /** Save All information **/
    public void onBackPressed() {
        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        backActivity();
        super.onBackPressed();
    }

    private void backActivity() {

        // login state > go to AgentListActivity
        Intent intent = new Intent(this, SettingPageActivity.class);
        GroupInfo group = AgentCommon.getParentInfo(GlobalStatic.g_agentInfo.groupid);
        intent.putExtra("PARENTNAME", AgentCommon.getMostParentName());
        intent.putExtra("PARENTID", AgentCommon.getMostParentID());
        intent.putExtra("REFRESH", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        startLeftSlideAnimation();

        finish();
    }

    private void makeLogItemLists() {
        showProgressHandler(getString(R.string.remotepc_loading_waiting_message));
        new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                logListArrayList.clear();
                ArrayList<String> datas = sqlHelper.getDatas(AgentSQLiteHelper.LOG_TABLE, 3);
                for (int i = datas.size() - 3; i > 0; i = i - 3) {
                    logListArrayList.add(new InfoListItem(Integer.parseInt(datas.get(i)), datas.get(i + 1), datas.get(i + 2), InfoListItem.NO_BUTTON_EVENT));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // com.rsupport.setting listview item in adapter
                        InfoItemAdapter = new InfoItemAdapter(AgentLogActivity.this, logListArrayList, false);
                        logList.setAdapter(InfoItemAdapter);
                        hideProgressHandler();
                        if (BottomTitle != null) {
                            if (logListArrayList.size() == 0) {
                                BottomTitle.setEnabled(false);
                            } else
                                BottomTitle.setEnabled(true);
                        }
                    }
                });
            }
        }).start();

    }

}
