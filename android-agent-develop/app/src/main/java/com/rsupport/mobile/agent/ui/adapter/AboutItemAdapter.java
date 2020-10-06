package com.rsupport.mobile.agent.ui.adapter;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.repo.config.ProxyInfo;
import com.rsupport.mobile.agent.ui.base.InfoListItem;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity;

import org.koin.java.KoinJavaComponent;

import java.util.ArrayList;

public class AboutItemAdapter extends InfoListItemAdapter {


    public static final int SUB_ID_PROXY_ADDR = 61;
    public static final int SUB_ID_PROXY_PORT = 62;
    public static final int SUB_ID_PROXY_ID = 63;
    public static final int SUB_ID_PROXY_PW = 64;
    public static final int SUB_ID_AGENT_INSTALL_ID = 65;
    public static final int SUB_ID_AGENT_INSTALL_PW = 66;
    public static final int SUB_ID_AGENT_INSTALL_PW_RE = 67;
    public static final int SUB_ID_AGENT_INSTALL_NAME = 68;

    public AboutItemAdapter(RVCommonActivity context, ArrayList<InfoListItem> itemArray) {
        super(context, itemArray);
    }

    @Override
    protected void setEditText(View convertView, InfoListItem listItem, final int position) {
        final EditText editText = (EditText) convertView.findViewById(R.id.item_content_edit);
        editText.setText(mItemArray.get(position).getItemContent());
        editText.setVisibility(View.VISIBLE);

        InfoListItem infoListItem = mItemArray.get(position);

        if (infoListItem instanceof TextWatcher) {
            editText.addTextChangedListener((TextWatcher) infoListItem);
        }
        // port 입력칸은 숫자만
        if (infoListItem.getChildID() == SUB_ID_PROXY_PORT) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }

        // 비밀번호 입력칸은 ***
        if (infoListItem.getChildID() == SUB_ID_PROXY_PW || infoListItem.getChildID() == SUB_ID_AGENT_INSTALL_PW || infoListItem.getChildID() == SUB_ID_AGENT_INSTALL_PW_RE) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }


        // 하단 divider 없을 경우, 하단 마진 없음
        if (infoListItem.isDivider == false && position < mItemArray.size() - 1) {
            LinearLayout lay = (LinearLayout) convertView.findViewById(R.id.item_left_layout);
            lay.setPadding(lay.getPaddingLeft(), lay.getPaddingTop(), lay.getPaddingRight(), 0);
        }

        editText.addTextChangedListener(new TextWatcher() {


            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {

                String editValue = arg0.toString().trim();
                InfoListItem infoListItem = mItemArray.get(position);
                switch (infoListItem.getChildID()) {
                    case SUB_ID_AGENT_INSTALL_NAME:
                        GlobalStatic.g_agentInstallNAME = editValue;
                        break;
                    case SUB_ID_AGENT_INSTALL_ID:
                        GlobalStatic.g_agentInstallID = editValue;
                        break;
                    case SUB_ID_AGENT_INSTALL_PW:
                        GlobalStatic.g_agentInstallPasswd = editValue;
                        break;
                    case SUB_ID_AGENT_INSTALL_PW_RE:
                        GlobalStatic.g_agentInstallPasswdRe = editValue;
                        break;
                    case SUB_ID_PROXY_ADDR: {
                        ConfigRepository configRepository = KoinJavaComponent.get(ConfigRepository.class);
                        ProxyInfo proxyInfo = configRepository.getProxyInfo();
                        configRepository.setProxyInfo(proxyInfo.copy(editValue, proxyInfo.getPort(), proxyInfo.getId(), proxyInfo.getPwd()));
                    }
                    break;
                    case SUB_ID_PROXY_PORT: {
                        ConfigRepository configRepository = KoinJavaComponent.get(ConfigRepository.class);
                        ProxyInfo proxyInfo = configRepository.getProxyInfo();
                        configRepository.setProxyInfo(proxyInfo.copy(proxyInfo.getAddress(), editValue, proxyInfo.getId(), proxyInfo.getPwd()));
                    }
                    break;
                    case SUB_ID_PROXY_ID: {
                        ConfigRepository configRepository = KoinJavaComponent.get(ConfigRepository.class);
                        ProxyInfo proxyInfo = configRepository.getProxyInfo();
                        configRepository.setProxyInfo(proxyInfo.copy(proxyInfo.getAddress(), proxyInfo.getPort(), editValue, proxyInfo.getPwd()));
                    }
                    break;
                    case SUB_ID_PROXY_PW: {
                        ConfigRepository configRepository = KoinJavaComponent.get(ConfigRepository.class);
                        ProxyInfo proxyInfo = configRepository.getProxyInfo();
                        configRepository.setProxyInfo(proxyInfo.copy(proxyInfo.getAddress(), proxyInfo.getPort(), proxyInfo.getId(), editValue));
                    }
                    break;
                }
            }
        });
    }

    @Override
    protected void setToggle(View convertView, final ImageView imageBtnView, final InfoListItem listItem, final int position) {
        super.setToggle(convertView, imageBtnView, listItem, position);
    }

    @Override
    protected void setCheck(View convertView, InfoListItem listItem, TextView textView) {
        super.setCheck(convertView, listItem, textView);

        // checkbox & content Layout (For padding area control)
        LinearLayout lay = (LinearLayout) convertView.findViewById(R.id.item_left_layout);

        // 첫 항목일 경우 상단 padding 높이 다름
        int top_size = 0;
//		if(listItem.getmEventID() == AboutActivity.EVENT_ID_PRODUCT_PERSONAL) {
//			top_size = (int) lay.getResources().getDimension(R.dimen.padding_12);
//		}

        // 마지막 항목일 경우 하단 padding 높이 다름
        int bottom_size = (int) lay.getResources().getDimension(R.dimen.padding_4);
//		if(listItem.getmEventID() == BasicSettingActivity.EVENT_ID_PRODUCT_SERVER) {
//			bottom_size = (int) lay.getResources().getDimension(R.dimen.padding_12);
//		}

        lay.setPadding(lay.getPaddingLeft(), top_size, lay.getPaddingRight(), bottom_size);
    }
}
