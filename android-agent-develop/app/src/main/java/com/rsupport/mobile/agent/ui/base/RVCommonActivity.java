package com.rsupport.mobile.agent.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.api.model.AgentInfo;
import com.rsupport.mobile.agent.ui.dialog.RVDialog;
import com.rsupport.mobile.agent.ui.dialog.TransProgressDialog;
import com.rsupport.mobile.agent.ui.views.RVToast;

import java.util.ArrayList;
import java.util.List;

/**
 * FileName: RVCommonActivity.java
 * Author  : khkim
 * Date    : 2014. 2. 7
 * Purpose : Common fuction for RemoteView Activity
 * <p>
 * [History]
 */
public class RVCommonActivity extends CommonActivity {

    /**
     * title layout
     **/
    private LinearLayout titleLayout = null;
    /**
     * titlebar Left button
     **/
    private ImageButton leftButton = null;
    /**
     * titlebar Right button
     **/
    private ImageButton rightButton = null;
    /**
     * Title Status
     **/
    private TextView TitleTextView = null;

    private String toastMessage = null;
    private int duration;
    private Context context = null;

    String currentTitle = null;
    boolean isLeftButton = false;
    boolean isRightButton = false;

    /**
     * title layout (scroll body layout)
     **/
    protected LinearLayout BottomTitle = null;

    // common Progress bar
    protected TransProgressDialog m_progressDialog = null;
    protected Handler progressHandler = new Handler();
    protected Handler toastHandler = new Handler();
    protected Handler alertHandler = new Handler();

    protected RVDialog dialog = null;

    public RVCommonActivity() {
        context = this;
    }

    @Override
    protected void onStop() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }

        if (m_progressDialog != null) {
            m_progressDialog.dismiss();
            m_progressDialog = null;
        }

        super.onStop();
    }

    public Context getContext() {
        return context;
    }


    /**
     * Basic BackGround layout_common_bg_ns_no_margin
     **/
    public void setContentView(int layoutResID, int backgroundLayoutResID) {
        super.setContentView(backgroundLayoutResID);
        getLayoutInflater().inflate(layoutResID, (ViewGroup) findViewById(R.id.contents_linearlayout));
    }

    /**
     * Basic BackGround layout_common_bg_ns_no_margin
     **/
    public <T extends ViewDataBinding> T setContentViewBinding(int layoutResID, int backgroundLayoutResID) {
        super.setContentView(backgroundLayoutResID);
        return DataBindingUtil.inflate(getLayoutInflater(), layoutResID, findViewById(R.id.contents_linearlayout), true);
    }


    /**
     * set Activity Background color
     **/
    public void setBackgroundColorRes(int colorResorce) {
        ViewGroup mainLayout = (ViewGroup) findViewById(R.id.main_layout);
        mainLayout.setBackgroundResource(colorResorce);
    }

    /**
     * set Activity Index Layout (only include scroll view layout)
     **/
    public void setIndexOnScrollView(boolean b_show) {
        ViewGroup indexLayout = (ViewGroup) findViewById(R.id.background_index_layout);

        // index 는 스크롤뷰 배경화면에만 있음. 없는 경우는 할당 불가능
        if (indexLayout != null) {
            if (b_show) {
                indexLayout.setVisibility(View.VISIBLE);
            } else {
                indexLayout.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Title 영역에  설정 (Basic mode don't have title)
     *
     * @param currentTitle  - 2단계 Path name
     * @param isLeftButton  - 메뉴 버튼이 보여질지 여부
     * @param isRightButton - 도움말 버튼이 보여질지 여부
     */
    public void setTitle(String currentTitle, boolean isLeftButton, boolean isRightButton) {
        this.currentTitle = currentTitle;
        this.isLeftButton = isLeftButton;
        this.isRightButton = isRightButton;
        pathRefresh();
    }

    public void setTitle(int currentTitleRecNum, boolean isLeftButton, boolean isRightButton) {
        setTitle(getString(currentTitleRecNum), isLeftButton, isRightButton);
    }

    public TextView getTitleTextView() {
        return TitleTextView;
    }

    public void hideTitle() {
        if (titleLayout == null) {
            titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        }

        titleLayout.setVisibility(View.GONE);
    }

    public LinearLayout getTitleLayout() {
        if (titleLayout == null) {
            titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        }
        return titleLayout;
    }

    private void pathRefresh() {
        if (stringCheckValue(currentTitle) == true) {
            if (titleLayout == null) {
                titleLayout = (LinearLayout) findViewById(R.id.title_layout);
            }
            if (titleLayout != null) {
                titleLayout.setVisibility(View.VISIBLE);
            }

            if (TitleTextView == null) {
                TitleTextView = (TextView) findViewById(R.id.common_state);
            }
            if (TitleTextView != null) {
                TitleTextView.setText(currentTitle);
                TitleTextView.setVisibility(View.VISIBLE);
            }
        }
        if (isLeftButton == true) {
            if (leftButton == null) {
                leftButton = (ImageButton) findViewById(R.id.left_button);
                if (leftButton != null) {
                    leftButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isSelected = onTitleItemClickEvent(v);
                            leftButton.setSelected(isSelected);
                        }
                    });
                }
            }
            if (leftButton != null) {
                leftButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (leftButton != null) {
                leftButton.setVisibility(View.INVISIBLE);
            }
        }

        if (isRightButton == true) {
            if (rightButton == null) {
                rightButton = (ImageButton) findViewById(R.id.right_button);
                if (rightButton != null) {
                    rightButton.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isSelected = onTitleItemClickEvent(v);
                            rightButton.setSelected(isSelected);
                        }
                    });
                }
            }

            if (rightButton != null) {
                rightButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (rightButton != null) {
                rightButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void setLeftButtonBackground(int resid) {
        leftButton.setBackgroundResource(resid);
    }

    public void setRightButtonBackground(int resid) {
        rightButton.setBackgroundResource(resid);
    }

    public void setBottomTitle(int bottomTitleText, Intent intent) {
        BottomTitle = (LinearLayout) findViewById(R.id.bottom_title_layout);
        BottomTitle.setVisibility(View.VISIBLE);

        TextView bottomTitle = (TextView) findViewById(R.id.bottom_title_text);
        bottomTitle.setText(bottomTitleText);
    }

    /**
     * 타이틀 아이템이 눌리면 호출
     *
     * @param view
     * @return selected 상태.
     */
    public boolean onTitleItemClickEvent(View view) {
        return false;
    }

    /**
     * 유효성 검사.
     *
     * @param value
     * @return - true(정상), false(비정상)
     */
    protected final boolean stringCheckValue(String value) {
        if (value == null || value.length() < 0) {
            return false;
        }
        return true;
    }

    /**
     * 이벤트 전달.
     * Activity 에서 호출한 타 class 에서 이벤트를 전달받을 때 사용
     * 필요에 따라 커스텀 사용
     */
    public void eventDelivery(int event) {
        return;
    }

    /**
     * List용
     */
    public void eventDelivery(int event, int pos) {
        return;
    }

    /**
     * Nothing message Progress Handler
     **/
    public void showProgressHandler() {
        progressHandler.post(new ProgressShow());
    }

    /**
     * message Progress Handler
     **/
    public void showProgressHandler(String waitString) {
        progressHandler.post(new ProgressShow(waitString));
    }

    /**
     * hide Progress Handler
     **/
    public void hideProgressHandler() {
        progressHandler.post(setProgressHide);
    }

    class ProgressShow implements Runnable {
        private String message = null;

        ProgressShow() {
        }

        ProgressShow(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            m_progressDialog = TransProgressDialog.show(context, null, message, true, true, null);
        }
    }

    private Runnable setProgressHide = new Runnable() {
        @Override
        public void run() {
            if (m_progressDialog != null && m_progressDialog.isShowing()) {
                m_progressDialog.dismiss();
                m_progressDialog = null;
            }
        }
    };

    public boolean isProgressShowing() {
        if (m_progressDialog == null) return false;
        return m_progressDialog.isShowing();
    }

    private RVDialog.Builder createDialogBuilder(String title, String message, int style) {
        RVDialog.Builder builder = new RVDialog.Builder(context);
        builder.setStyle(RVDialog.STYLE_NOTICE);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setStyle(style);

        return builder;
    }

    /**
     * parameter String Title, String Message(Content), int DialogStyle, String Button Text, EVENTID
     **/
    public void showAlertDialog(final String title, final String message, final int style, final int confirmText, final int confirmEventID) {

        alertHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    RVDialog.Builder builder = createDialogBuilder(title, message, style);
                    builder.setConfirm(context.getString(confirmText), confirmEventID);

                    dialog = builder.create(RVDialog.TYPE_ONE_BUTTON);
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * parameter String Title, String Message(Content), int DialogStyle, String Button Text, EVENTID
     **/
    public void showAlertDialog(final String title, final String message, final int style, final int confirmText, final int confirmEventID, final boolean cancel) {

        alertHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    RVDialog.Builder builder = createDialogBuilder(title, message, style);
                    builder.setConfirm(context.getString(confirmText), confirmEventID);

                    dialog = builder.create(RVDialog.TYPE_ONE_BUTTON);
                    dialog.setCancelable(cancel);
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showListDialog(final String title, final ArrayList<AgentInfo> itemList, final int confirmText, final int confirmEventID) {

        alertHandler.post(new Runnable() {
            @Override
            public void run() {
                RVDialog.Builder builder = createDialogBuilder(title, "", RVDialog.STYLE_LIST);

                builder.setConfirm(context.getString(confirmText), confirmEventID);
                CommonArrayAdapter adapter = new CommonArrayAdapter(context, R.layout.dialog_amtpower_list_item, R.id.gateway_name, itemList);
                builder.setAdapter(adapter);

                dialog = builder.create(RVDialog.TYPE_ONE_BUTTON);
                dialog.show();
            }
        });
    }

    /**
     * parameter String Title, String Message(Content), int DialogStyle, String Button Text, String Button Text
     **/
    public void showAlertDialog(final String title, final String message, final int style,
                                final int positiveText, final int positiveEventID, final int negativeText, final int btnNegatEventID) {
        alertHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    RVDialog.Builder builder = createDialogBuilder(title, message, style);

                    builder.setPositive(context.getString(positiveText), positiveEventID);
                    builder.setNegative(context.getString(negativeText), btnNegatEventID);
                    if (dialog != null) dialog.dismiss();
                    dialog = builder.create(RVDialog.TYPE_TWO_BUTTON);
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * parameter String Title, String Message(Content), int DialogStyle, String Button Text, String Button Text
     **/
    public void showAlertDialog(final String title, final String message, final int style,
                                final int positiveText, final int positiveEventID, final int negativeText, final int btnNegatEventID, final boolean cancel) {
        alertHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    RVDialog.Builder builder = createDialogBuilder(title, message, style);

                    builder.setPositive(context.getString(positiveText), positiveEventID);
                    builder.setNegative(context.getString(negativeText), btnNegatEventID);
                    if (dialog != null) dialog.dismiss();
                    dialog = builder.create(RVDialog.TYPE_TWO_BUTTON);
                    dialog.setCanceledOnTouchOutside(cancel);
                    dialog.setCancelable(cancel);
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    RVToast toast;

    /**
     * show Toast
     **/
    public void showToast(String strContent, int duration) {
        toastMessage = strContent;
        this.duration = duration;
        toastHandler.post(setToastShow);
    }

    private Runnable setToastShow = new Runnable() {
        @Override
        public void run() {
            try {
                if (toast != null)
                    toast.cancel();
            } catch (Exception e) {
            }
            toast = RVToast.makeText(context, toastMessage, duration);
            toast.show();
        }
    };

    public void startLeftSlideAnimation() {
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void startRightSlideAnimation() {
        overridePendingTransition(android.R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public RVDialog getRVDialog() {
        return dialog;
    }

    public void hideShadowLine() {
        findViewById(R.id.shadow_line).setVisibility(View.GONE);
    }

    public class CommonArrayAdapter extends ArrayAdapter<AgentInfo> {
        private ArrayList<AgentInfo> itemList;
        private int itemLayoutId;
        private Context context;

        public CommonArrayAdapter(Context context, int resource,
                                  int textViewResourceId, List objects) {
            super(context, resource, textViewResourceId, objects);
            itemList = (ArrayList<AgentInfo>) objects;
            itemLayoutId = resource;
            this.context = context;
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public AgentInfo getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            TextView gatewayName;
            TextView gatewayIp;
            ImageView chkGateway;

            if (v == null) {
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(itemLayoutId, null);
            }

            gatewayName = (TextView) v.findViewById(R.id.gateway_name);
            gatewayName.setText(itemList.get(position).name);
            gatewayIp = (TextView) v.findViewById(R.id.gateway_ip);
            gatewayIp.setText(itemList.get(position).localip);
            chkGateway = (ImageView) v.findViewById(R.id.chk_gateway);

            return v;
        }

    }

    protected void hideKeyboard(EditText editField) {
        InputMethodManager m_inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        m_inputManager.hideSoftInputFromWindow(editField.getWindowToken(), 0);
    }
}
