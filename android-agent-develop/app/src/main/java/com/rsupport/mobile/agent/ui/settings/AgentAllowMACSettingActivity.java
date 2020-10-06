package com.rsupport.mobile.agent.ui.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.utils.Utility;

import java.util.ArrayList;

import com.rsupport.mobile.agent.api.model.GroupInfo;
import com.rsupport.mobile.agent.utils.AgentCommon;
import com.rsupport.mobile.agent.ui.adapter.InfoItemAdapter;
import com.rsupport.mobile.agent.ui.base.InfoListItem;
import com.rsupport.mobile.agent.ui.adapter.InfoListItemAdapter;
import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.utils.AgentSQLiteHelper;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.util.log.RLog;

@SuppressLint("NewApi")
public class AgentAllowMACSettingActivity extends RVCommonActivity {

    InfoItemAdapter InfoItemAdapter = null;

    ListView macList = null;
    EditText etMac = null;
    TextView tvListTitle = null;
    String macPattern = "([a-fA-F0-9]{1,12})";
    ArrayList<InfoListItem> macListArrayList = new ArrayList<InfoListItem>();
    AgentSQLiteHelper sqlHelper = null;
    int beforeCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.agent_allow_mac_setting_activity, R.layout.layout_common_bg_margin);

        setTitle(R.string.agent_allow_mac, true, false);
        setLeftButtonBackground(R.drawable.button_headerback);
        findViewById(R.id.copyright).setVisibility(View.GONE);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);


        macList = (ListView) findViewById(R.id.about_list1);
        etMac = (EditText) findViewById(R.id.et_mac);
        etMac.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int start, int before, int count) {
                beforeCount = count - before;
            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = s.toString();
                if (!text.matches(macPattern)) {
                    if (s.length() > 0 && beforeCount > 0) {
                        s.delete(s.length() - beforeCount, s.length());
                        etMac.setText(s.toString());
                        etMac.setSelection(s.length());
                    }
                }
            }
        });
        tvListTitle = (TextView) findViewById(R.id.tv_list_title);
        ImageView addBtn = (ImageView) findViewById(R.id.btn_mac_add);

        addBtn.setOnClickListener(view -> {
            //dbAdd etMAc;
            itemAdd(etMac.getText().toString().toUpperCase());
        });
        createHelpLink();

        addBtn.bringToFront();
        sqlHelper = new AgentSQLiteHelper(this);
        sqlHelper.initDB();

        btnTitleLeft.setOnClickListener(v -> {
            hideKeyboard(etMac);
            backActivity();
        });

    }

    private void createHelpLink() {
        TextView tv_Win = (TextView) findViewById(R.id.os_win);
        TextView tv_Mac = (TextView) findViewById(R.id.os_mac);
        TextView tv_Android = (TextView) findViewById(R.id.os_android);
        TextView tv_iOS = (TextView) findViewById(R.id.os_ios);

        String windows = tv_Win.getText().toString();
        String mac = tv_Mac.getText().toString();
        String android = tv_Android.getText().toString();
        String iOS = tv_iOS.getText().toString();

        String pre_add = "<a href=https://content.rview.com/";
        String post_add = "/faq-items";
        String languageCode = Utility.getSystemLanguage(this);

        String win_add = "/how-to-set-macaddress-access-windows>";
        String mac_add = "/how-to-set-macaddress-access-mac>";
        String ios_add = "/how-to-set-macaddress-access-ios>";
        String android_add = "/how-to-set-macaddress-access-android>";

        tv_Win.setText(Html.fromHtml(pre_add + languageCode + post_add + win_add + windows));
        tv_Mac.setText(Html.fromHtml(pre_add + languageCode + post_add + mac_add + mac));
        tv_Android.setText(Html.fromHtml(pre_add + languageCode + post_add + android_add + android));
        tv_iOS.setText(Html.fromHtml(pre_add + languageCode + post_add + ios_add + iOS));

        tv_Win.setMovementMethod(LinkMovementMethod.getInstance());
        tv_Mac.setMovementMethod(LinkMovementMethod.getInstance());
        tv_Android.setMovementMethod(LinkMovementMethod.getInstance());
        tv_iOS.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    protected void onResume() {
        super.onResume();

        makeMacItemLists();

    }

    @Override
    /** Save All information **/
    public void onBackPressed() {
        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);
        backActivity();
        super.onBackPressed();
    }

    private void backActivity() {

        Intent intent = new Intent(this, AgentSettingActivity.class);
        GroupInfo group = AgentCommon.getParentInfo(GlobalStatic.g_agentInfo.groupid);
        intent.putExtra("PARENTNAME", AgentCommon.getMostParentName());
        intent.putExtra("PARENTID", AgentCommon.getMostParentID());
        intent.putExtra("REFRESH", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        startLeftSlideAnimation();

        finish();
    }

    private void makeMacItemLists() {
        macListArrayList.clear();
        ArrayList<String> datas = sqlHelper.getDatas(AgentSQLiteHelper.MAC_TABLE, 2);
        for (int i = 0; i < datas.size(); i = i + 2) {
            macListArrayList.add(new InfoListItem(Integer.parseInt(datas.get(i)), null, datas.get(i + 1), InfoListItem.REMOVE_ITEM));
        }

        if (getResources().getConfiguration().locale.getLanguage().toLowerCase().equals("ja")) {
            findViewById(R.id.address_help_layout).setVisibility(View.GONE);
        } else if (datas.size() > 0) {
            findViewById(R.id.address_help_layout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.address_help_layout).setVisibility(View.VISIBLE);
        }


        InfoItemAdapter = new InfoItemAdapter(this, macListArrayList, false);
        macList.setAdapter(InfoItemAdapter);
        tvListTitle.setVisibility(macListArrayList.size() == 0 ? View.INVISIBLE : View.VISIBLE);
        InfoListItemAdapter.getListViewSize(macList);
    }

    private boolean itemAdd(String getMac) {


        if (getMac.matches(macPattern)) {
            ArrayList<String> data = new ArrayList<String>();
            data.add(getMac);
            sqlHelper.insertData(AgentSQLiteHelper.MAC_TABLE, data);
            etMac.setText("");
            makeMacItemLists();
        } else {
            RLog.d("noMatch");
            showAlertDialog(null, getString(R.string.agent_allow_mac_input_porm), RVDialog.STYLE_NOTICE, R.string.common_ok, -10);
            return false;
        }
        return true;
    }

    @Override
    public void eventDelivery(int event) {
        if (dialog != null) {
            dialog.dismiss();
        }
        int removeId = event;
        RLog.d("REMOVE!!!! : " + event);
        sqlHelper.deleteData(AgentSQLiteHelper.MAC_TABLE, event);
        makeMacItemLists();
    }
}
