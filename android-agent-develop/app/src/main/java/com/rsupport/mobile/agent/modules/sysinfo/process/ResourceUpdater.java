package com.rsupport.mobile.agent.modules.sysinfo.process;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;


import com.rsupport.util.log.RLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResourceUpdater {

    private Context context;
    private ProcessCache processCache;
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public ResourceUpdater(Context context, ProcessCache processCache) {
        this.context = context;
        this.processCache = processCache;
    }

    public List<ProcessItem> update() {
        PackageManager pm = context.getPackageManager();
        boolean changed = false;
        ArrayList<ProcessItem> localList = processCache.generateLocalList();

        for (int i = 0, size = localList.size(); i < size; i++) {
            if (isClosed.get()) break;

            ProcessItem processItem = localList.get(i);
            String pkgName = processItem.getRunningAppInfo().getPkgName();
            if (processCache.hasCached(pkgName)) {
                continue;
            }
            changed = updateLabel(pm, changed, processItem, pkgName);
            updateIcon(pm, processItem, pkgName);
            processCache.putCache(pkgName, processItem);
        }
        RLog.i("ResourceUpdaterThread_end");
        orderByNameIfChanged(changed);
        return localList;
    }

    private void updateIcon(PackageManager pm, ProcessItem processItem, String packageName) {
        if (processItem.getIcon() != null) {
            return;
        }

        try {
            android.content.pm.ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);

            if (ai != null) {
                try {
                    Drawable icon = pm.getApplicationIcon(ai);
                    if (icon == null) {
                        icon = pm.getDefaultActivityIcon();
                    }
                    processItem.setIcon(icon);
                } catch (OutOfMemoryError oom) {
                    RLog.e("OOM when loading : " + ai.packageName + " : " + oom.getLocalizedMessage());
                }
            }
        } catch (PackageManager.NameNotFoundException e1) {
            // ignore this exception
        }
    }

    private void orderByNameIfChanged(boolean changed) {
        if (changed) {
            processCache.orderByName();
        }
    }

    private boolean updateLabel(PackageManager pm, boolean changed, ProcessItem proc, String pname) {
        try {
            android.content.pm.ApplicationInfo ai = pm.getApplicationInfo(pname, 0);
            if (ai != null) {
                CharSequence label = pm.getApplicationLabel(ai);
                if (label != null) {
                    proc.setLabel(label.toString());
                    changed = true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            int idx = pname.indexOf(':');
            if (idx != -1) {
                String name = pname.substring(0, idx);
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(name, 0);

                    if (ai != null) {
                        CharSequence label = pm.getApplicationLabel(ai);

                        if (label != null) {
                            proc.setLabel(label.toString() + pname.substring(idx));
                            changed = true;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e1) {
                    // ignore this exception
                }
            }
        }
        return changed;
    }

    public void close() {
        isClosed.set(true);
    }
}
