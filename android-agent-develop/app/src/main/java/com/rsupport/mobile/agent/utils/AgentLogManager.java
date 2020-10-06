package com.rsupport.mobile.agent.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AgentLogManager {
    public synchronized void addAgentLog(Context context, String msg) {
        AgentSQLiteHelper sqlHelper = new AgentSQLiteHelper(context);
        sqlHelper.initDB();
        ArrayList<String> list = new ArrayList<String>();
        list.add(new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(new Date(System.currentTimeMillis())));
        list.add(msg);
        sqlHelper.insertData(AgentSQLiteHelper.LOG_TABLE, list);
        sqlHelper.close();
    }
}
