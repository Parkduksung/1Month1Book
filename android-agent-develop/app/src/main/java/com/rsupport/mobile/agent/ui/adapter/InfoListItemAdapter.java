package com.rsupport.mobile.agent.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;

import java.util.ArrayList;

import com.rsupport.mobile.agent.ui.base.RVCommonActivity;
import com.rsupport.mobile.agent.ui.base.InfoListItem;

public class InfoListItemAdapter extends BaseAdapter {
    RVCommonActivity mContext = null;

    protected ArrayList<InfoListItem> mItemArray = null;
    ;

    private int mlistItemsSize = 0;

    String https = "https://";
    String http = "http://";

    public InfoListItemAdapter(RVCommonActivity context, ArrayList<InfoListItem> itemArray) {
        mContext = context;
        mItemArray = itemArray;

        // 리스트뷰에 들어가는 아이템 갯수
        mlistItemsSize = itemArray.size();
    }

    @Override
    public int getCount() {
        return mItemArray.size();
    }

    @Override
    public String getItem(int pos) {
        return mItemArray.get(pos).getItemTitle();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final InfoListItem listItem = mItemArray.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (listItem.isIncludeLayout) {
                convertView = inflater.inflate(listItem.getIncludeLayoutResID(), parent, false);
                setIncludeLayout(convertView);
                return convertView;
            }
            convertView = inflater.inflate(R.layout.layout_setting_item, parent, false);
        }

        if (listItem.isIncludeLayout) {
            return convertView;
        }

        // If first item position, change to press image
        if (position == 0) {
            convertView.setBackgroundResource(R.drawable.listgroup_item_top);
        }

        // If Last Item position, change to press image
        if (position == (mlistItemsSize - 1)) {
            convertView.setBackgroundResource(R.drawable.listgroup_item_bottom);
            listItem.isDivider = false;
        }

        // If items size is only 1, change to press image
        if (mlistItemsSize == 1) {
            convertView.setBackgroundResource(R.drawable.listgroup_item_single);
        }

        TextView tvItemTitle = (TextView) convertView.findViewById(R.id.item_title);
        if (listItem.getItemTitle() != null) {
            tvItemTitle.setText(mItemArray.get(position).getItemTitle());
        } else {
            tvItemTitle.setVisibility(View.GONE);
        }

        TextView tvItemContent = (TextView) convertView.findViewById(R.id.item_content);
        tvItemContent.setText(mItemArray.get(position).getItemContent());

        ImageView imageBtnView = (ImageView) convertView.findViewById(R.id.imgBtn);
        if (listItem.isRightButton) {
            setRightButton(convertView, imageBtnView);
        } else if (listItem.isDownButton) {
            setDownButton(convertView, imageBtnView);
        } else if (listItem.isUpButton) {
            setUpButton(convertView, imageBtnView);
        } else if (listItem.isToggleOn || listItem.isToggleOff) {
            setToggle(convertView, imageBtnView, listItem, position);
        } else if (listItem.isEditBox) {
            tvItemContent.setVisibility(View.GONE);
            setEditText(convertView, listItem, position);
        } else if (listItem.isCheckImage) {
            setCheck(convertView, listItem, tvItemContent);
        } else if (listItem.isCopyImage) {
            setCopy(convertView, imageBtnView);
        } else if (listItem.isKillItem) {
            setKillItem(convertView, imageBtnView);
        } else if (listItem.isRemoveItem) {
            setRemoveItem(convertView, imageBtnView);
            setEvent(imageBtnView, position);
        }
        if (listItem.isRadiobutton) {
            setRadioButton(convertView);
        }

        ImageView imgNew = (ImageView) convertView.findViewById(R.id.imgNew);
        if (listItem.isNewIcon) {
            imgNew.setVisibility(View.VISIBLE);
        } else {
            imgNew.setVisibility(View.GONE);
        }

        if (listItem.isEvent) {
            setEvent(convertView, position);
        } else {
            LinearLayout layoutItem = (LinearLayout) convertView.findViewById(R.id.setting_item);
            layoutItem.setBackgroundResource(R.drawable.list_selector_color_transparent);
            if (listItem.isToggleOn || listItem.isToggleOff) {

            } else {
                layoutItem.setEnabled(false);
            }

            // item press 효과 방지
            tvItemTitle.setTextColor(layoutItem.getResources().getColor(R.color.color05));
            tvItemContent.setTextColor(layoutItem.getResources().getColor(R.color.color02));
        }

        /* Hide to division line */
        if (!listItem.isDivider) {
            ViewGroup layoutItem = (ViewGroup) convertView.findViewById(R.id.div_horizon);
            layoutItem.setVisibility(View.GONE);
        }

        return convertView;
    }

    private int getFrameAniTime(AnimationDrawable frameAnimation) {
        int animationTime = 0;
        for (int frameNum = 0; frameNum < frameAnimation.getNumberOfFrames(); frameNum++) {
            animationTime += frameAnimation.getDuration(frameNum);
        }
        return animationTime;
    }

    /**
     * scrollview에 listview가 들어갈 시에 listview 높이 적용
     */
    public static int getListViewSize(ListView myListView) {

        ListAdapter myListAdapter = (ListAdapter) myListView.getAdapter();
        if (myListAdapter == null) {
            return 0;
        }

        // set listAdapter in loop for getting final size
        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(myListView.getWidth(), MeasureSpec.UNSPECIFIED);

        // Add item view size
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        // com.rsupport.setting listview item in adapter
        ViewGroup.LayoutParams params = myListView.getLayoutParams();

        // Add divider size
        // scroll 방지 여유영역을 준다. +1
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount())) + 1;

        myListView.setLayoutParams(params);

        myListView.requestLayout();

        return totalHeight;
    }

    public static int getListViewSizeLog(ListView myListView) {

        ListAdapter myListAdapter = (ListAdapter) myListView.getAdapter();
        if (myListAdapter == null) {
            return 0;
        }

        // set listAdapter in loop for getting final size
        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(myListView.getWidth(), MeasureSpec.UNSPECIFIED);

        // Add item view size
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        return totalHeight;
    }

    protected void setRightButton(View convertView, ImageView imageBtnView) {
        imageBtnView.setImageResource(R.drawable.button_arrowright);
        imageBtnView.setVisibility(View.VISIBLE);
    }

    protected void setDownButton(View convertView, ImageView imageBtnView) {
        imageBtnView.setImageResource(R.drawable.button_arrowdown);
        imageBtnView.setVisibility(View.VISIBLE);
    }

    protected void setUpButton(View convertView, ImageView imageBtnView) {
        imageBtnView.setImageResource(R.drawable.button_arrowup);
        imageBtnView.setVisibility(View.VISIBLE);
    }

    protected void setCopy(View convertView, ImageView imageBtnView) {
        imageBtnView.setImageResource(R.drawable.button_clipboard);
        imageBtnView.setVisibility(View.VISIBLE);
    }

    protected void setCheck(View convertView, InfoListItem listItem, TextView textView) {
        ImageView imageView = (ImageView) convertView.findViewById(R.id.item_check);

        // checkbox & content Layout (For padding area control)
        LinearLayout lay = (LinearLayout) convertView.findViewById(R.id.item_left_layout);

        LinearLayout layoutItem = (LinearLayout) convertView.findViewById(R.id.setting_item);
        layoutItem.setBackgroundResource(R.drawable.list_selector_color_transparent);

        textView.setHeight((int) layoutItem.getResources().getDimension(R.dimen.langage_textview_height));

        LinearLayout layoutItemContent = (LinearLayout) convertView.findViewById(R.id.item_content_layout);

        if (listItem.getModeOn()) {
            imageView.setVisibility(View.VISIBLE);
            textView.setTextColor(layoutItem.getResources().getColor(R.color.color04));
            layoutItemContent.setBackgroundResource(R.drawable.bg_dialoguegroup_select);
        } else {
            imageView.setVisibility(View.INVISIBLE);
            layoutItemContent.setBackgroundResource(R.drawable.list_selector_checkbox);
        }
    }

    protected void setToggle(View convertView, final ImageView imageBtnView, final InfoListItem listItem, final int position) {
        if (listItem.isToggleOn) {
            imageBtnView.setBackgroundResource(R.drawable.toggle_on);
        } else {
            imageBtnView.setBackgroundResource(R.drawable.toggle_off);
        }

        imageBtnView.setVisibility(View.VISIBLE);
        imageBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listItem.isToggleOn) {
                    imageBtnView.setBackgroundResource(R.drawable.toggle_animation_on);
                } else {
                    imageBtnView.setBackgroundResource(R.drawable.toggle_animation_off);
                }

                AnimationDrawable frameAnimation = (AnimationDrawable) imageBtnView.getBackground();
                frameAnimation.start();

                // After Playing animation Go to Event
                int animationTime = getFrameAniTime(frameAnimation);

                Handler delayHandler = new Handler();
                delayHandler.postDelayed(new Runnable() {
                    public void run() {
                        int eventID = mItemArray.get(position).getmEventID();
                        mContext.eventDelivery(eventID);
                    }
                }, animationTime);
            }
        });
    }

    protected void setKillItem(View convertView, ImageView imageBtnView) {
        imageBtnView.setImageResource(R.drawable.button_kill_item);
        imageBtnView.setVisibility(View.VISIBLE);
    }

    protected void setRemoveItem(View convertView, ImageView imageBtnView) {
        imageBtnView.setImageResource(R.drawable.button_remove_item);
        imageBtnView.setVisibility(View.VISIBLE);
    }

    protected void setEditText(View convertView, final InfoListItem listItem, final int position) {
        final EditText editText = (EditText) convertView.findViewById(R.id.item_content_edit);
        editText.setText(mItemArray.get(position).getItemContent());
        editText.setVisibility(View.VISIBLE);
        if (mItemArray.get(position).isPassword) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                setRadioButton(convertView);
            }
        });
    }

    private void setRadioButton(View convertView) {
        RadioGroup radioGroup = (RadioGroup) convertView.findViewById(R.id.radio_http);
        RadioButton radioButtonHttps = (RadioButton) convertView.findViewById(R.id.https);
        RadioButton radioButtonHttp = (RadioButton) convertView.findViewById(R.id.http);
        EditText editText = (EditText) convertView.findViewById(R.id.item_content_edit);
        radioButtonHttps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSSLURL(editText, true);
            }
        });
        radioButtonHttp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSSLURL(editText, false);
            }
        });

        String url = editText.getText().toString();
        radioGroup.setVisibility(View.VISIBLE);


        if (url.startsWith(https)) {
            radioButtonHttps.setChecked(true);
        } else if (url.startsWith(http)) {
            radioButtonHttp.setChecked(true);
        } else {
            int idx = url.indexOf("://");
            String newurl;

            if (idx >= 0) {
                newurl = "https" + url.substring(idx);
            } else {
                newurl = "https://" + url;
            }

            url = newurl;
            radioButtonHttps.setChecked(true);
        }
        editText.setText(url);

    }

    private void setSSLURL(EditText editText, boolean isSSL) {
        String scheme = "";

        if (isSSL) {
            scheme = "https";
        } else {
            scheme = "http";
        }

        String currentUrl = editText.getText().toString();
        int idx = currentUrl.indexOf("://");

        if (idx >= 0) {
            currentUrl = scheme + currentUrl.substring(idx);
        } else {
            currentUrl = scheme + "://" + currentUrl;
        }
        editText.setText(currentUrl);
    }

    protected void setIncludeLayout(View convertView) {

    }

    protected void setEvent(View convertView, final int position) {
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int eventID = mItemArray.get(position).getmEventID();
                mContext.eventDelivery(eventID);
            }
        });
    }
}
