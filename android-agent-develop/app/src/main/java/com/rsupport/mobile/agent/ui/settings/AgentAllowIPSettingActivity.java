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
public class AgentAllowIPSettingActivity extends RVCommonActivity {

    /**
     * AboutSetting Activity Information ListBox
     **/
    InfoItemAdapter InfoItemAdapter = null;

    /**
     * AboutSetting Activity Information ListBox View
     **/
    ListView ipList = null;
    EditText etStartIP = null;
    EditText etEndIP = null;
    TextView tvListTitle = null;

    /**
     * AboutSetting Activity Information ListArray
     **/
    ArrayList<InfoListItem> ipListArrayList = new ArrayList<InfoListItem>();
    AgentSQLiteHelper sqlHelper = null;
    int beforeCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.agent_allow_ip_setting_activity, R.layout.layout_common_bg_margin);

        setTitle(R.string.agent_allow_ip, true, false);
        setLeftButtonBackground(R.drawable.button_headerback);
        findViewById(R.id.copyright).setVisibility(View.GONE);

        ImageButton btnTitleLeft = (ImageButton) findViewById(R.id.left_button);


        ipList = (ListView) findViewById(R.id.about_list1);
        etStartIP = (EditText) findViewById(R.id.et_start_ip);
        etEndIP = (EditText) findViewById(R.id.et_end_ip);
        etStartIP.addTextChangedListener(twListner);
        etEndIP.addTextChangedListener(twListner);
        tvListTitle = (TextView) findViewById(R.id.tv_list_title);
        ImageView addBtn = (ImageView) findViewById(R.id.btn_mac_add);
        createHelpLink();
        addBtn.setOnClickListener(view -> {
            itemAdd(etStartIP.getText().toString(), etEndIP.getText().toString());
        });

        btnTitleLeft.setOnClickListener(view -> {
            hideKeyboard(etStartIP);
            backActivity();
        });

        addBtn.bringToFront();
        sqlHelper = new AgentSQLiteHelper(this);
        sqlHelper.initDB();
    }

    @Override
    protected void onResume() {
        super.onResume();

        makeIPItemLists();

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

        String win_add = "/how-to-set-windows-access-ip>";
        String mac_add = "/how-to-set-macosx-access-ip>";
        String android_add = "/how-to-set-android-access-ip>";
        String ios_add = "/how-to-set-ios-access-ip>";

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

    private void makeIPItemLists() {
        ipListArrayList.clear();
        ArrayList<String> datas = sqlHelper.getDatas(AgentSQLiteHelper.IP_TABLE, 4);
        for (int i = 0; i < datas.size(); i = i + 4) {
            if (datas.get(i + 1).equals("end")) {
                ipListArrayList.add(new InfoListItem(Integer.parseInt(datas.get(i)), null, datas.get(i + 2) + " ~ " + datas.get(i + 3), InfoListItem.REMOVE_ITEM));
            } else {
                ipListArrayList.add(new InfoListItem(Integer.parseInt(datas.get(i)), null, datas.get(i + 2), InfoListItem.REMOVE_ITEM));
            }
        }
        if (getResources().getConfiguration().locale.getLanguage().toLowerCase().equals("ja")) {
            findViewById(R.id.address_help_layout).setVisibility(View.GONE);
        } else if (datas.size() > 0) {
            findViewById(R.id.address_help_layout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.address_help_layout).setVisibility(View.VISIBLE);
        }


        InfoItemAdapter = new InfoItemAdapter(this, ipListArrayList, false);
        ipList.setAdapter(InfoItemAdapter);
        tvListTitle.setVisibility(ipListArrayList.size() == 0 ? View.INVISIBLE : View.VISIBLE);
        InfoListItemAdapter.getListViewSize(ipList);
    }

    private boolean itemAdd(String startIP, String endIP) {

        String ipPattern = "([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})";
        RLog.d("startIP : " + startIP + ", endIP : " + endIP);
        ArrayList<String> data = new ArrayList<String>();

        if (startIP.matches(ipPattern)) {
            if (endIP.equals("")) {
                data.add("start");
                data.add(startIP);
                data.add("");
            } else {
                if (endIP.matches(ipPattern)) {
                    if (ipRangeCheck(startIP, endIP)) {
                        data.add("end");
                        data.add(startIP);
                        data.add(endIP);
                    } else {
                        showAlertDialog(null, getString(R.string.agent_allow_ip_input_range), RVDialog.STYLE_NOTICE, R.string.common_ok, -10);
                        return false;
                    }
                } else {
                    showAlertDialog(null, getString(R.string.agent_allow_ip_input_porm), RVDialog.STYLE_NOTICE, R.string.common_ok, -10);
                    return false;
                }

            }
            sqlHelper.insertData(AgentSQLiteHelper.IP_TABLE, data);
            etStartIP.setText("");
            etEndIP.setText("");
            makeIPItemLists();
        } else {
            showAlertDialog(null, getString(R.string.agent_allow_ip_input_porm), RVDialog.STYLE_NOTICE, R.string.common_ok, -10);
            RLog.d("noMatch");
            return false;
        }
        return true;
    }

    public boolean ipRangeCheck(String start, String end) {
        boolean ret = false;
        String[] startIP = start.split("\\.");
        String[] endIP = end.split("\\.");

        String startIPSum = String.format("%d%03d%03d%03d", Integer.parseInt(startIP[0]), Integer.parseInt(startIP[1]), Integer.parseInt(startIP[2]), Integer.parseInt(startIP[3]));
        String endIPSum = String.format("%d%03d%03d%03d", Integer.parseInt(endIP[0]), Integer.parseInt(endIP[1]), Integer.parseInt(endIP[2]), Integer.parseInt(endIP[3]));
        long startIPret = Long.parseLong(startIPSum);
        long endIPret = Long.parseLong(endIPSum);
        if (startIPret <= endIPret) {
            ret = true;
        }
        return ret;
    }


    @Override
    public void eventDelivery(int event) {
        if (dialog != null) {
            dialog.dismiss();
        }

        if (event == -10) {

        } else {
            int removeId = event;
            RLog.d("REMOVE!!!! : " + event);
            sqlHelper.deleteData(AgentSQLiteHelper.IP_TABLE, event);
            makeIPItemLists();
        }

    }

    TextWatcher twListner = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int start, int before, int count) {
            beforeCount = count - before;
        }

        @Override
        public void afterTextChanged(Editable s) {
            String ipPattern = "([0-9.]{1,15})";
            String text = s.toString();
            RLog.d("beforeCount : " + beforeCount + " , text : " + text);
            if (!text.matches(ipPattern)) {
                if (s.length() > 0 && beforeCount > 0) {
                    s.delete(s.length() - beforeCount, s.length());
                    if (etStartIP.isFocused()) {
                        etStartIP.setText(s.toString());
                        etStartIP.setSelection(s.length());
                    } else if (etEndIP.isFocused()) {
                        etEndIP.setText(s.toString());
                        etEndIP.setSelection(s.length());
                    }
                }

            }
        }
    };
}
