package com.rsupport.mobile.agent.ui.settings;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.rsupport.mobile.agent.api.ApiService;
import com.rsupport.mobile.agent.utils.Result;
import com.rsupport.mobile.agent.R;
import com.rsupport.rscommon.exception.RSException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.rsupport.mobile.agent.api.model.GroupInfo;
import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;

import org.koin.core.Koin;
import org.koin.java.KoinJavaComponent;

/**
 * Created by tonykim on 15. 6. 19..
 */
public class GroupSelectActivity extends RVCommonActivity {

    private EditText searchEdit;
    private ListView groupList;
    private TextView naviTextview;
    private AgentGroupListAdapter adapter;
    private ImageButton searchBtn;
    private GroupInfo groupInfo;
    private Vector<GroupInfo> mGroupList;
    private Button selectBtn;
    private ArrayList<GroupInfo> groupParentsInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.grouplist, R.layout.layout_common_bg_ns_no_margin);

        setTitle(R.string.agent_install_group_name, true, false);

        setLeftButtonBackground(R.drawable.button_headerback);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        btnTitleLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchEdit = (EditText) findViewById(R.id.search_edit);
        groupList = (ListView) findViewById(R.id.group_list);
        searchBtn = (ImageButton) findViewById(R.id.search_btn);
        naviTextview = (TextView) findViewById(R.id.navi_text);
        selectBtn = (Button) findViewById(R.id.group_select);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishSelectGroup();
            }
        });
        adapter = new AgentGroupListAdapter(this, R.layout.groupcontext, GlobalStatic.g_vecGroups);
        groupList.setAdapter(adapter);
        groupParentsInfo.clear();
        setNaviTextView();
        setEvent();
    }

    private void setNaviTextView() {
        StringBuilder sb = new StringBuilder();
        sb.append("\\");
        for (GroupInfo info : groupParentsInfo) {
            sb.append(Html.fromHtml(info.getGroupName()));
            sb.append("\\");
        }
        naviTextview.setText(sb.toString());
    }

    private void setEvent() {
        groupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                groupInfo = (GroupInfo) adapter.getItem(position);
                groupParentsInfo.add(groupInfo);
                new getGroupList().execute(groupInfo.grpid);

            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GroupDataSearchTask().execute(searchEdit.getText().toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        RLog.i("groupParentsInfo size : " + groupParentsInfo.size());
        searchEdit.setText("");
        if (groupParentsInfo.size() == 1) {
            adapter = new AgentGroupListAdapter(this, R.layout.groupcontext, GlobalStatic.g_vecGroups);
            groupList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            grouplistViewCheck(GlobalStatic.g_vecGroups);
            groupParentsInfo.clear();
            setNaviTextView();
        } else if (groupParentsInfo.size() > 1) {
            new getGroupList().execute(groupParentsInfo.get(groupParentsInfo.size() - 2).grpid);
            groupParentsInfo.remove(groupParentsInfo.size() - 1);
        } else if (groupParentsInfo.size() == 0) {
            super.onBackPressed();
        }
    }

    private void finishSelectGroup() {

        if (adapter != null && adapter.getSelectPosition() != -1) {
            Intent intent = new Intent();
            GroupInfo info = adapter.getItem(adapter.getSelectPosition());
            intent.putExtra("groupId", info.grpid);
            intent.putExtra("groupName", info.grpname);
            GlobalStatic.g_agentInstallGroupID = info.grpid;
            GlobalStatic.g_agentInstallGroupNAME = info.grpname;
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    private void grouplistViewCheck(Vector list) {
        if (list.size() == 0) {
            findViewById(R.id.searchagentlayout).setVisibility(View.GONE);
            findViewById(R.id.group_list).setVisibility(View.GONE);
            findViewById(R.id.no_folder).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.searchagentlayout).setVisibility(View.VISIBLE);
            findViewById(R.id.group_list).setVisibility(View.VISIBLE);
            findViewById(R.id.no_folder).setVisibility(View.GONE);
        }

    }

    private class getGroupList extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            showProgressHandler(getString(R.string.remotepc_loading_waiting_message));
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... params) {
            Result<List<GroupInfo>> agentSubListResult = KoinJavaComponent.get(ApiService.class).getAgentSubGroupList("1", params[0]);
            mGroupList = new Vector<>();
            if (agentSubListResult instanceof Result.Success) {
                mGroupList.addAll(((Result.Success<List<GroupInfo>>) agentSubListResult).getValue());
            }
            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] strings) {
            grouplistViewCheck(mGroupList);

            adapter = new AgentGroupListAdapter(GroupSelectActivity.this, R.layout.groupcontext, mGroupList);
            groupList.setAdapter(adapter);
            setNaviTextView();
            hideProgressHandler();
            super.onPostExecute(strings);
        }
    }


    private class GroupDataSearchTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            showProgressHandler(getString(R.string.remotepc_loading_waiting_message));
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... string) {

            final String searchText = string[0];

            String groupId = "";
            if (groupParentsInfo.size() > 0) {
                GroupInfo pGroupInfo = groupParentsInfo.get(groupParentsInfo.size() - 1);
                groupId = pGroupInfo.pgrpid;
            }

            Result<List<GroupInfo>> groupSearchResult = KoinJavaComponent.get(ApiService.class).getGroupSearch("1", GlobalStatic.connectionInfo.ctype, searchText, groupId);
            mGroupList = new Vector<>();
            if (groupSearchResult instanceof Result.Success) {
                mGroupList.addAll(((Result.Success<List<GroupInfo>>) groupSearchResult).getValue());
            }

            if (mGroupList == null) {
                String msg = "";

                if (GlobalStatic.g_err.trim().length() > 0) {
                    msg = GlobalStatic.g_err;
                } else {
                    msg = GlobalStatic.errMessageProc(GroupSelectActivity.this);

                }

                showAlertDialog(null, msg, RVDialog.STYLE_NOTICE, R.string.common_ok, -1);
            }

            return new String[0];
        }

        @Override
        protected void onPostExecute(String[] strings) {

            hideProgressHandler();

            grouplistViewCheck(mGroupList);

            adapter = new AgentGroupListAdapter(GroupSelectActivity.this, R.layout.groupcontext, mGroupList);
            groupList.setAdapter(adapter);
            hideProgressHandler();

            super.onPostExecute(strings);
        }
    }

    public void eventDelivery(int event) {

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

    }
}
