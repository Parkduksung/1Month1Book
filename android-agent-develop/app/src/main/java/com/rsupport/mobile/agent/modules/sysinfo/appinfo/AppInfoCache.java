package com.rsupport.mobile.agent.modules.sysinfo.appinfo;

import com.rsupport.mobile.agent.modules.sysinfo.appinfo.AppInfoHolder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * AppCache
 */
public class AppInfoCache {
    private ArrayList<AppInfoHolder> appInfoHolders = new ArrayList<>();

    private ArrayList<AppInfoHolder> allAppInfoHolders = new ArrayList<>();
    public HashMap<String, AppInfoHolder> appLookup = new HashMap<>();

    public AppInfoCache() {
    }

    public ArrayList<AppInfoHolder> getAppInfoHolders() {
        return appInfoHolders;
    }

    public synchronized ArrayList<AppInfoHolder> generateLocalList() {
        return new ArrayList<>(allAppInfoHolders);
    }

    public synchronized void update() {
        allAppInfoHolders.retainAll(appInfoHolders);
        for (int i = 0, size = appInfoHolders.size(); i < size; i++) {
            AppInfoHolder ai = appInfoHolders.get(i);
            AppInfoHolder oai = appLookup.get(ai.appInfo.packageName);

            if (oai == null) {
                oai = ai;
                appLookup.put(ai.appInfo.packageName, ai);
            } else {
                oai.appInfo = ai.appInfo;
                oai.version = ai.version;
                if (oai.version.length() > 10) {
                    oai.version = oai.version.toString().substring(0, 10);
                }
                oai.isPrivate = ai.isPrivate;
                oai.checked = ai.checked;
                oai.versionCode = ai.versionCode;
            }

            if (!allAppInfoHolders.contains(oai)) {
                allAppInfoHolders.add(oai);
            }
        }
    }

    public synchronized void clear() {
        appInfoHolders.clear();
        allAppInfoHolders.clear();
        appLookup.clear();
    }

    public void close() {
        clear();
    }
}
