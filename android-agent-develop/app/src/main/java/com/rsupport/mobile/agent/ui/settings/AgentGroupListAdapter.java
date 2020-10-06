package com.rsupport.mobile.agent.ui.settings;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rsupport.mobile.agent.R;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.rsupport.mobile.agent.api.model.GroupInfo;
import com.rsupport.util.log.RLog;

public class AgentGroupListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private Vector<GroupInfo> mGroupInfoList;
    private SelectedImageView selectedImageView;
    private int mLayoutId;

    class SelectedImageView {
        public ImageView mImegeView = null;
        public int position;
        public boolean status = false;

        public void setmImegeView(ImageView view, boolean status, int position) {
            mImegeView = view;
            this.status = status;
            this.position = position;
        }
    }

    public AgentGroupListAdapter(Context context, int layout, Vector<GroupInfo> arSrc) {
        this.context = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGroupInfoList = arSrc;
        mLayoutId = layout;
        if (mGroupInfoList.size() > 1) {
            Collections.sort(mGroupInfoList, groupKeySort);
        }
        selectedImageView = new SelectedImageView();
    }

    public int getCount() {
        return mGroupInfoList.size();
    }

    public GroupInfo getItem(int position) {
        return mGroupInfoList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // final int pos = position;

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutId, parent, false);
        }
        final ImageView folderCheck = (ImageView) convertView.findViewById(R.id.img_folder_chek);
        folderCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroupInfoList.get(position).isSelect = !mGroupInfoList.get(position).isSelect;

                folderImageChange(folderCheck, position, mGroupInfoList.get(position).isSelect);
            }
        });
        TextView groupName = (TextView) convertView.findViewById(R.id.tvgroupname);
        groupName.setText(Html.fromHtml(mGroupInfoList.get(position).getGroupName()));

        return convertView;
    }

    public static Comparator<GroupInfo> groupKeySort = new Comparator<GroupInfo>() {

        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(GroupInfo leftItem, GroupInfo rightItem) {
            int ret = -1;

            if (convertNumber(leftItem.key) > convertNumber(rightItem.key)) {
                ret = 1;
            } else if (convertNumber(leftItem.key) == convertNumber(rightItem.key)) {
                ret = 0;
            }

            return ret;
        }

        private int convertNumber(String key) {
            return Integer.parseInt(key.replace("GROUP", ""));
        }
    };

    private void setForderImageChange(ImageView view, boolean status) {
        if (status) {
            view.setBackgroundResource(R.drawable.folders_check);
        } else {
            view.setBackgroundResource(R.drawable.folders_uncheck);
        }
    }

    public int getSelectPosition() {
        if (selectedImageView.status) {
            return selectedImageView.position;
        }
        return -1;
    }


    private void folderImageChange(ImageView newView, int position, boolean status) {

        RLog.i("folderImageChange ::: " + newView + " Status ::: " + status);

        if (selectedImageView.mImegeView != null) {
            setForderImageChange(selectedImageView.mImegeView, false);
//            mGroupInfoList.get(selectedImageView.position).isSelect = false;
        }
        newView.setTag(status);
        setForderImageChange(newView, status);
        selectedImageView.setmImegeView(newView, status, position);

    }
}
