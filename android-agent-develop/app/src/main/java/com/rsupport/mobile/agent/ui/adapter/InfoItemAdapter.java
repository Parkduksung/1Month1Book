package com.rsupport.mobile.agent.ui.adapter;

import java.util.ArrayList;

import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.base.InfoListItem;

import android.view.View;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;

public class InfoItemAdapter extends InfoListItemAdapter {

    public String localip = null;
    public String globalip = null;
    public String macaddress = null;
    public String lastaccesstim = null;
    public String regtime = null;
    public String subnetmask = null;
    public String lastuser = null;

    private boolean mWolMode = false;

    public InfoItemAdapter(RVCommonActivity context, ArrayList<InfoListItem> itemArray, boolean wolMode) {
        super(context, itemArray);

        mWolMode = wolMode;
    }

    @Override
    protected void setIncludeLayout(View convertView) {
        ((TextView) convertView.findViewById(R.id.text_localip)).setText(localip);
        ((TextView) convertView.findViewById(R.id.text_macaddress)).setText(macaddress);
        ((TextView) convertView.findViewById(R.id.text_regtime)).setText(regtime);
        ((TextView) convertView.findViewById(R.id.text_subnetmask)).setText(subnetmask);
        ((TextView) convertView.findViewById(R.id.text_lastuser)).setText(lastuser);

        if (mWolMode == false) {
            ((TextView) convertView.findViewById(R.id.text_globalip)).setText(globalip);
            ((TextView) convertView.findViewById(R.id.text_lastaccesstime)).setText(lastaccesstim);
        } else {
            convertView.findViewById(R.id.globalip_layout).setVisibility(View.GONE);
            convertView.findViewById(R.id.lastaccesstime_layout).setVisibility(View.GONE);
        }
    }
}
