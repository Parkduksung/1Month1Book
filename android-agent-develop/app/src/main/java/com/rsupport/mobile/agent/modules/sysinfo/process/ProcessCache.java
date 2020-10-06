package com.rsupport.mobile.agent.modules.sysinfo.process;

import androidx.annotation.Nullable;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ProcessCache {

    private HashMap<String, ProcessItem> cacheMap = new HashMap<>();
    private ArrayList<ProcessItem> processItems = new ArrayList<>();

    synchronized ArrayList<ProcessItem> generateLocalList() {
        return new ArrayList<>(processItems);
    }

    public ArrayList<ProcessItem> getProcessItems() {
        return processItems;
    }

    synchronized void orderByName() {
        Collections.sort(processItems, new Comparator<ProcessItem>() {
            Collator clt = Collator.getInstance();

            public int compare(ProcessItem obj1, ProcessItem obj2) {
                String lb1 = obj1.getLabel() == null ? obj1.getRunningAppInfo().getPkgName() : obj1.getLabel();
                String lb2 = obj2.getLabel() == null ? obj2.getRunningAppInfo().getPkgName() : obj2.getLabel();
                return clt.compare(lb1, lb2);
            }
        });
    }

    public boolean hasCached(String pkgName) {
        return cacheMap.containsKey(pkgName);
    }

    public void putCache(String pkgName, ProcessItem processItem) {
        cacheMap.put(pkgName, processItem);
    }

    public void clear() {
        processItems.clear();
    }

    public void add(ProcessItem processItem) {
        processItems.add(processItem);
    }

    public @Nullable
    ProcessItem getCached(String name) {
        return cacheMap.get(name);
    }

    public @Nullable
    ProcessItem findProcessItem(String targetName) {
        for (int i = 0; i < processItems.size(); i++) {
            ProcessItem pi = processItems.get(i);
            String processName;
            if (pi.getLabel() != null) {
                processName = pi.getLabel();
            } else {
                processName = pi.getRunningAppInfo().getPkgName();
            }
            if (targetName.equals(processName)) {
                return pi;
            }
        }
        return null;
    }

    public void close() {
        cacheMap.clear();
        processItems.clear();
    }
}
