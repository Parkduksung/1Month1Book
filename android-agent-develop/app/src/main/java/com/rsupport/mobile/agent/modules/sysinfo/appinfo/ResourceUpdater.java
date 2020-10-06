package com.rsupport.mobile.agent.modules.sysinfo.appinfo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;


import com.rsupport.util.log.RLog;

import java.util.ArrayList;

/**
 * ResourceUpdaterThread
 */
public class ResourceUpdater {

    private Context ac;
    private AppInfoCache appInfoCache;

    private volatile boolean aborted;

    public ResourceUpdater(Context ac, AppInfoCache appInfoCache) {
        this.ac = ac;
        this.appInfoCache = appInfoCache;
    }

    public void update() {
        android.content.pm.ApplicationInfo ai;
        AppInfoHolder holder;
        PackageManager pm = ac.getPackageManager();

        ArrayList<AppInfoHolder> localList = appInfoCache.generateLocalList();

        for (int i = 0, size = localList.size(); i < size; i++) {
            if (aborted) {
                return;
            }
            ai = localList.get(i).appInfo;
            CharSequence label = ai.loadLabel(pm);
            holder = appInfoCache.appLookup.get(ai.packageName);

            if (holder != null) {
                synchronized (appInfoCache) {
                    holder.label = label;
                }
            }
        }
        for (int i = 0, size = localList.size(); i < size; i++) {
            if (aborted) {
                return;
            }
            ai = localList.get(i).appInfo;
            try {
                Drawable icon = ai.loadIcon(pm);
                holder = appInfoCache.appLookup.get(ai.packageName);

                if (holder != null) {
                    holder.icon = icon;
                }
            } catch (OutOfMemoryError oom) {
                RLog.e("OOM when loading : " + oom.getLocalizedMessage());
            }
        }
    }

    public void close() {
        aborted = true;
    }
}
