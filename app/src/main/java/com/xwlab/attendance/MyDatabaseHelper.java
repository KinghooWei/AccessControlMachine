package com.xwlab.attendance;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    // 创建Book数据表语法
    public static final String CREATE_BOOK = "create table FaceInfo (" +
            "id integer primary key autoincrement, " +
            "name text, " +
            "feature text," +
            "feature_with_mask text," +
            "feature_with_glasses text," +
            "phoneNum text," +
            "password text" +
            ")";

    // 通过构造方法创建数据库，其中name为数据库名称
    public MyDatabaseHelper(Context context, String name) {
        super(context, name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行创建数据表的语法
        db.execSQL(CREATE_BOOK);
        Log.i("Database", "create database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO 更改数据库版本的操作
    }

}