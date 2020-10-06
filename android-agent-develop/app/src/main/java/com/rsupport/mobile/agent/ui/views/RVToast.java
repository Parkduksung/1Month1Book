package com.rsupport.mobile.agent.ui.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rsupport.mobile.agent.R;

public class RVToast extends Toast {

    public RVToast(Context context) {
        super(context);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) ((Activity) context).findViewById(R.id.toast_layout_root));

        setView(layout);

        // setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        // setDuration(Toast.LENGTH_LONG);
    }

    public static RVToast makeText(Context context, CharSequence text, int duration) {
        RVToast result = new RVToast(context);

        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.toast, null);
        TextView tv = (TextView) v.findViewById(R.id.toast_message);
        tv.setText(text);

        result.setView(v);
        result.setDuration(duration);

        return result;
    }
}
