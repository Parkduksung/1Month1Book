package com.rsupport.mobile.agent.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;

import com.rsupport.mobile.agent.ui.base.CommonActivity;
import com.rsupport.mobile.agent.ui.base.RVCommonActivity.CommonArrayAdapter;

/*******************************************************************************
 * ______ _____ __ __ _____ _____ _____ ______ _______ / ___ | / ____| / / / //
 * __ | / ___ | / __ | / ___ ||___ __| / /__/ / | |____ / / / // / | |/ / | |/ /
 * | | / /__/ / / / / ___ | |____ |/ / / // /__/ // /__/ / | | | |/ ___ | / / /
 * / | | ____| || |__/ // ____// ____/ | |_/ // / | | / / /_/ |_| |_____/
 * |_____//__/ /__/ |____//_/ |_| /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2012 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * RSUPPORT Company Limited and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to RSUPPORT Company
 * Limited and its suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material is
 * strictly forbidden unless prior written permission is obtained from RSUPPORT
 * Company Limited.
 *
 * FileName: RVDialog.java Author : khkim Date : 2014. 3. 11. 
 * Purpose : Dialog
 *
 */
public class RVDialog extends Dialog {
    /**
     * 알림 타입
     **/
    public static final int STYLE_NOTICE = 0;
    /**
     * 입력 타입
     **/
    public static final int STYLE_COMMENT = 1;

    public static final int STYLE_LIST = 2;

    /**
     * 버튼 타입
     **/
    public static final int TYPE_ONE_BUTTON = 1;
    public static final int TYPE_TWO_BUTTON = 2;

    private static EditText editTextView = null;
    private static ListView editListView = null;

    public RVDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    public RVDialog(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog);

        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public String getEditText() {
        String text = null;
        if (editTextView != null) {
            text = editTextView.getText().toString();
        }
        return text;
    }

    public EditText getEditView() {
        return editTextView;
    }

    public static class Builder {

        private Context context;
        private String mTitle;
        private String mMessage;
        private String mConfirmButtonText;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private CommonArrayAdapter mAdapter;

        private int mConfirmEventID;
        private int mPositiveEventID;
        private int mNegativeEventID;

        private int mStyle = STYLE_NOTICE;

        private TextView mConfirmView;
        private TextView mPositiveView;
        private TextView mNegativeView;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setStyle(int style) {
            this.mStyle = style;
            return this;
        }

        public Builder setMessage(String message) {
            this.mMessage = message;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setConfirm(String positiveButtonText, int eventID) {
            this.mConfirmButtonText = positiveButtonText;
            this.mConfirmEventID = eventID;
            return this;
        }

        public Builder setPositive(String positiveButtonText, int eventID) {
            this.mPositiveButtonText = positiveButtonText;
            this.mPositiveEventID = eventID;
            return this;
        }

        public Builder setNegative(String negativeButtonText, int eventID) {
            this.mNegativeButtonText = negativeButtonText;
            this.mNegativeEventID = eventID;
            return this;
        }

        public void setAdapter(CommonArrayAdapter adapter) {
            this.mAdapter = adapter;
        }

        public RVDialog create(int buttonType) {
            final RVDialog dialog = new RVDialog(context, R.style.Theme_BorderlessDialog);
            dialog.setCanceledOnTouchOutside(false);

            if (buttonType == TYPE_ONE_BUTTON) {
                mConfirmView = (TextView) dialog.findViewById(R.id.confirm);
                mConfirmView.setVisibility(View.VISIBLE);
                mConfirmView.setText(mConfirmButtonText);
                mConfirmView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ((CommonActivity) context).eventDelivery(mConfirmEventID);
                    }
                });

            } else if (buttonType == TYPE_TWO_BUTTON) {
                mPositiveView = (TextView) dialog.findViewById(R.id.accept);
                mPositiveView.setVisibility(View.VISIBLE);
                mPositiveView.setText(mPositiveButtonText);
                mPositiveView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ((CommonActivity) context).eventDelivery(mPositiveEventID);
                    }
                });

                ViewGroup div_line = (ViewGroup) dialog.findViewById(R.id.div_vertical);
                div_line.setVisibility(View.VISIBLE);

                mNegativeView = (TextView) dialog.findViewById(R.id.reject);
                mNegativeView.setVisibility(View.VISIBLE);
                mNegativeView.setText(mNegativeButtonText);
                mNegativeView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ((CommonActivity) context).eventDelivery(mNegativeEventID);
                    }
                });
            }

            if (mTitle == null || "".equals(mTitle)) {
                // 팝업 타이틀 내용 없을 경우, 타이틀바 비움
                ViewGroup popTitleLayout = (ViewGroup) dialog.findViewById(R.id.popup_title);
                popTitleLayout.setVisibility(View.GONE);
            } else {
                TextView titleTextView = (TextView) dialog.findViewById(R.id.tvtitle);
                titleTextView.setText(mTitle);
            }

            TextView contentTextView = (TextView) dialog.findViewById(R.id.tvcontent);
            if (mMessage.equals("")) {
                contentTextView.setVisibility(View.GONE);
            }
            contentTextView.setText(mMessage);

            if (mStyle == STYLE_COMMENT) {
                editTextView = (EditText) dialog.findViewById(R.id.popup_edit);
                editTextView.setVisibility(View.VISIBLE);
            } else if (mStyle == STYLE_LIST) {
                editListView = (ListView) dialog.findViewById(R.id.popup_list);
                editListView.setAdapter(mAdapter);
                editListView.setVisibility(View.VISIBLE);
                dialog.findViewById(R.id.dialog_middle_content).setPadding(0, 0, 0, 0);

                editListView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,
                                            int pos, long id) {
                        ((CommonActivity) context).eventDelivery(mConfirmEventID, pos);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
//			dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//				@Override
//				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//					if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
////						if (mNegativeView != null && mNegativeView.getVisibility() == View.VISIBLE) {
////							mNegativeView.performClick();
////						} else if (mConfirmView != null && mConfirmView.getVisibility() == View.VISIBLE) {
////							mConfirmView.performClick();
////						} else {
//							dialog.dismiss();
////						}
//						return true;
//					}
//					return false;
//				}
//			});
            return dialog;
        }

        public RVDialog create(int buttonType, final View.OnClickListener okClick, final View.OnClickListener cancelClick) {
            final RVDialog dialog = new RVDialog(context, R.style.Theme_BorderlessDialog);
            dialog.setCanceledOnTouchOutside(false);

            if (buttonType == TYPE_ONE_BUTTON) {
                mConfirmView = (TextView) dialog.findViewById(R.id.confirm);
                mConfirmView.setVisibility(View.VISIBLE);
                mConfirmView.setText(mConfirmButtonText);
                mConfirmView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        okClick.onClick(v);
                    }
                });

            } else if (buttonType == TYPE_TWO_BUTTON) {
                mPositiveView = (TextView) dialog.findViewById(R.id.accept);
                mPositiveView.setVisibility(View.VISIBLE);
                mPositiveView.setText(mPositiveButtonText);
                mPositiveView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        okClick.onClick(v);
                        dialog.dismiss();
                    }
                });

                ViewGroup div_line = (ViewGroup) dialog.findViewById(R.id.div_vertical);
                div_line.setVisibility(View.VISIBLE);

                mNegativeView = (TextView) dialog.findViewById(R.id.reject);
                mNegativeView.setVisibility(View.VISIBLE);
                mNegativeView.setText(mNegativeButtonText);
                mNegativeView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        cancelClick.onClick(v);
                        dialog.dismiss();
                    }
                });
            }

            if (mTitle == null || "".equals(mTitle)) {
                // 팝업 타이틀 내용 없을 경우, 타이틀바 비움
                ViewGroup popTitleLayout = (ViewGroup) dialog.findViewById(R.id.popup_title);
                popTitleLayout.setVisibility(View.GONE);
            } else {
                TextView titleTextView = (TextView) dialog.findViewById(R.id.tvtitle);
                titleTextView.setText(mTitle);
            }

            TextView contentTextView = (TextView) dialog.findViewById(R.id.tvcontent);
            if (mMessage.equals("")) {
                contentTextView.setVisibility(View.GONE);
            }
            contentTextView.setText(mMessage);

            if (mStyle == STYLE_COMMENT) {
                editTextView = (EditText) dialog.findViewById(R.id.popup_edit);
                editTextView.setVisibility(View.VISIBLE);
            }

//			dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
//				@Override
//				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//					if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
////						if (mNegativeView != null && mNegativeView.getVisibility() == View.VISIBLE) {
////							mNegativeView.performClick();
////						} else if (mConfirmView != null && mConfirmView.getVisibility() == View.VISIBLE) {
////							mConfirmView.performClick();
////						} else {
//							dialog.dismiss();
////						}
//						return true;
//					}
//					return false;
//				}
//			});
            return dialog;
        }
    }
}
