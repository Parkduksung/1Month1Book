package com.rsupport.mobile.agent.modules.sysinfo.appinfo;

import android.graphics.drawable.Drawable;

import com.rsupport.mobile.agent.modules.sysinfo.ApplicationInfo;
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppInfo;

import java.util.List;

/**
 * AppInfoHolder
 */
public final class AppInfoHolder {

    public android.content.pm.ApplicationInfo appInfo;
    public CharSequence label;
    public CharSequence version;
    public Drawable icon;
    public int versionCode;
    public boolean isPrivate;
    public boolean checked;
    public boolean enable;
    public boolean removable;

    public AppInfoHolder() {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AppInfoHolder)) {
            return false;
        }
        AppInfoHolder that = (AppInfoHolder) o;
        return this.appInfo.packageName.equals(that.appInfo.packageName);
    }

    private String getIdentifierFromAppInfoHolder(AppInfoHolder appInfoHolder) {
        String packageName;
        if (appInfoHolder.label != null) {
            packageName = appInfoHolder.label.toString();
        } else {
            packageName = appInfoHolder.appInfo.packageName;
        }
        return packageName;
    }

    public String createItemString(int appType, List<RunningAppInfo> raps) {
        String itemString = getIdentifierFromAppInfoHolder(this) + "&/";
        itemString += version + "&/";

        String state = "OFF";
        for (int j = 0, t = raps.size(); j < t; j++) {
            if (appInfo.processName.equals(raps.get(j).getPkgName())) {
                state = "ON";
                break;
            }
        }
        itemString += state + "&/";

        if (removable) {
            itemString += "1";
        } else {
            itemString += "0";
        }

        if (appType == ApplicationInfo.APP_TYPE_ALL) {
            if ((appInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
                itemString += "&/1";//system
            } else {
                itemString += "&/0";//user
            }
        }
        return itemString;
    }
}
