package com.geekband.Test.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/6/18.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    public static final int VERSION = 1;
    public static final String WEATHER_TABLE = "WeatherTable";


    // 用构造器创建数据库
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null , VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + WEATHER_TABLE + " (" +
                "saveTime text, " +
                "cityName text, " +
                "city text, " +
                "txt text, " +
                "tmp text, " +
                "date_0 text, " +
                "hum_0 text, " +
                "max_0 text, " +
                "min_0 text, " +
                "dir_0 text, " +
                "sc_0 text, " +
                "date_1 text, " +
                "hum_1 text, " +
                "txt_d_1 text, " +
                "txt_n_1 text, " +
                "max_1 text, " +
                "min_1 text, " +
                "dir_1 text, " +
                "sc_1 text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
