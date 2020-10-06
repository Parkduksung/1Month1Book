package com.rsupport.mobile.agent.utils;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AgentSQLiteHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "RV_AGENT_DATABASE";

    public static final String IP_TABLE = "IP_ALLOW_TABLE";
    public static final String MAC_TABLE = "PORT_ALLOW_TABLE";
    public static final String LOG_TABLE = "LOGS_TABLE";

    private SQLiteDatabase db = null; // DB controller

    public AgentSQLiteHelper(Context context) {
        super(context, DB_NAME, null, 1);

    }

    public void initDB() {
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // dbcreate

        String tableName = "";
        String sql = "CREATE  TABLE \"" + IP_TABLE + "\"(" + "'number' INTEGER NOT NULL," + "'option' TEXT NOT NULL," + "'startIP' TEXT," + "'endIP' TEXT," + "PRIMARY KEY (number) );";

        db.execSQL(sql);
        sql = "CREATE  TABLE \"" + MAC_TABLE + "\"(" + "'number' INTEGER NOT NULL," + "'port' TEXT," + "PRIMARY KEY (number) );";

        db.execSQL(sql);
        sql = "CREATE  TABLE \"" + LOG_TABLE + "\"(" + "'number' INTEGER NOT NULL," + "'date' TEXT," + "'text' TEXT," + "PRIMARY KEY (number) );";

        db.execSQL(sql);
//		Log.d("hyu", "onCreate :  " + sql);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // 데이터 추가
    public void insertData(String tableName, ArrayList<String> values) {
        String sql = "insert into " + tableName + " values(null, ";

        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            String data = (String) iterator.next();
            sql += "'" + data + "', ";
        }
        sql = sql.substring(0, sql.length() - 2) + ");";
//		Log.d("hyu", "insertData :  " + sql);
        db.execSQL(sql);
    }

    // 데이터 삭제
    public void deleteData(String tableName, int index) {
        String sql = "delete from " + tableName + " where number = " + index + ";";
//		Log.d("hyu", "deleteData :  " + sql);
        db.execSQL(sql);
    }

    // 데이터 전체 삭제
    public void deleteAllData(String tableName) {
        String sql = "delete from " + tableName + ";";
        sql += "update SQLITE_SEQUENCE set seq = 0 where name = '" + tableName + "';";
//		Log.d("hyu", "deleteAllData :  " + sql);
        db.execSQL(sql);
    }

    public void deleteAllTable() {
        deleteAllData(IP_TABLE);
        deleteAllData(MAC_TABLE);
        deleteAllData(LOG_TABLE);
    }

    // 데이터 추가
    public void updateData(String tableName, int index, String filde, String data) {
        String sql = "update " + tableName + " set " + filde + " = '" + data + "' where number = " + index + ";";
//		Log.d("hyu", "updateData :  " + sql);
        db.execSQL(sql);
    }

    public ArrayList<String> getDatas(String tableName, int colomNum) {
        ArrayList<String> list = new ArrayList<String>();
        String sql = "select * from " + tableName;
        Cursor result = db.rawQuery(sql, null);

        result.moveToFirst();

        while (!result.isAfterLast()) {
            for (int i = 0; i < colomNum; i++) {
                if (colomNum == 0) {
                    list.add("" + result.getInt(i));
                } else {
                    list.add(result.getString(i));
                }
            }
            result.moveToNext();
        }

        result.close();

        return list;
    }

}
