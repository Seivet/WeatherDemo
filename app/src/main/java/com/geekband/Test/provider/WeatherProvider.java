package com.geekband.Test.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.geekband.Test.database.DBHelper;

public class WeatherProvider extends ContentProvider{

    public static final String CONTENT ="content://";
    public static final String AUTHORITY ="com.geekband.Test";

    public static final String WeatherURI = CONTENT + AUTHORITY + "/" + DBHelper.WEATHER_TABLE;
    private static UriMatcher sUriMatcher;

    private DBHelper mDBHelper;

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext());
        return false;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, DBHelper.WEATHER_TABLE, 1);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)){
            return null;
        }

        Cursor cursor = mDBHelper.getReadableDatabase()
                .query(tableName, projection, selection, selectionArgs, null, null,sortOrder);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)){
            return null;
        }

        long id = mDBHelper.getReadableDatabase().insert(tableName, null, values);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)){
            return -1;
        }

        // the number of rows affected
        int count = mDBHelper.getReadableDatabase().delete(tableName, selection, selectionArgs);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)){
            return -1;
        }

        int count = mDBHelper.getReadableDatabase().update(tableName, values, selection, selectionArgs);
        return count;
    }

    // 封装
    private String getTableName(Uri uri) {

        String tableName = null;
        switch (sUriMatcher.match(uri)){
            case 1:
                tableName = DBHelper.WEATHER_TABLE;
                break;
        }
        return tableName;
    }
}
