package com.amk2.musicrunner.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.amk2.musicrunner.sqliteDB.MusicTrackDBHelper;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackCommonDataDB;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackRunningEventDataDB;

/**
 * Created by ktlee on 5/24/14.
 */
public class MusicTrackProvider extends ContentProvider {
    private static final int COMMON_DATA_DIR_INDICATOR  = 1;
    private static final int COMMON_DATA_ITEM_INDICATOR = 2;
    private static final int RUNNING_EVENT_DATA_DIR_INDICATOR  = 3;
    private static final int RUNNING_EVENT_DATA_ITEM_INDICATOR = 4;

    private MusicTrackDBHelper musicTrackDBHelper;
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MusicTrackMetaData.AUTHORITY, MusicTrackCommonDataDB.TABLE_NAME, COMMON_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicTrackMetaData.AUTHORITY, MusicTrackCommonDataDB.TABLE_NAME + "/#", COMMON_DATA_ITEM_INDICATOR);
        uriMatcher.addURI(MusicTrackMetaData.AUTHORITY, MusicTrackRunningEventDataDB.TABLE_NAME, RUNNING_EVENT_DATA_DIR_INDICATOR);
        uriMatcher.addURI(MusicTrackMetaData.AUTHORITY, MusicTrackRunningEventDataDB.TABLE_NAME + "/#", RUNNING_EVENT_DATA_ITEM_INDICATOR);
    }

    @Override
    public boolean onCreate() {
        musicTrackDBHelper = new MusicTrackDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();


        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicTrackCommonDataDB.TABLE_NAME);
                break;
            case COMMON_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicTrackCommonDataDB.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MusicTrackCommonDataDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
                break;
            case RUNNING_EVENT_DATA_DIR_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicTrackRunningEventDataDB.TABLE_NAME);
                break;
            case RUNNING_EVENT_DATA_ITEM_INDICATOR:
                sqLiteQueryBuilder.setTables(MusicTrackRunningEventDataDB.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MusicTrackRunningEventDataDB.COLUMN_NAME_ID + " = " + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI - " + uri.toString());
        }

        SQLiteDatabase readableDB = musicTrackDBHelper.getReadableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(readableDB, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        ContentValues cv;
        String TableName = null;
        Uri ContentUri = null;
        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:
                if (contentValues == null) {
                    cv = new ContentValues();
                } else {
                    cv = new ContentValues(contentValues);
                }
                if (!cv.containsKey(MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE)) {
                    throw new IllegalArgumentException("ContentValue must contain data type");
                }
                TableName = MusicTrackCommonDataDB.TABLE_NAME;
                ContentUri = MusicTrackCommonDataDB.CONTENT_URI;
                break;
            case RUNNING_EVENT_DATA_DIR_INDICATOR:
                if (contentValues == null) {
                    cv = new ContentValues();
                } else {
                    cv = new ContentValues(contentValues);
                }
                TableName = MusicTrackRunningEventDataDB.TABLE_NAME;
                ContentUri = MusicTrackRunningEventDataDB.CONTENT_URI;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI - " + uri);
        }
        SQLiteDatabase writableDB = musicTrackDBHelper.getWritableDatabase();
        long rowId = writableDB.insert(TableName, null, cv);
        if (rowId > 0) {
            Log.d("Provider Insert", "data is inserted in provider! with id=" + rowId);
            Uri addedUri = ContentUris.withAppendedId(ContentUri, rowId);
            this.getContext().getContentResolver().notifyChange(addedUri, null);
            return addedUri;
        }
        throw new IllegalArgumentException("Failed to insert data to uri - " + uri);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase writableDB = musicTrackDBHelper.getWritableDatabase();
        int cnt = -1;
        Uri updatedUri = null;
        switch (uriMatcher.match(uri)) {
            case COMMON_DATA_DIR_INDICATOR:
                cnt = writableDB.update(MusicTrackMetaData.MusicTrackCommonDataDB.TABLE_NAME, contentValues, selection, selectionArgs);
                updatedUri = ContentUris.withAppendedId(MusicTrackCommonDataDB.CONTENT_URI, Integer.parseInt(selectionArgs[0]));
                //this.getContext().getContentResolver().notifyChange(updatedUri, null);
                break;
            case RUNNING_EVENT_DATA_DIR_INDICATOR:
                cnt = writableDB.update(MusicTrackRunningEventDataDB.TABLE_NAME, contentValues, selection, selectionArgs);
                updatedUri = ContentUris.withAppendedId(MusicTrackRunningEventDataDB.CONTENT_URI, Integer.parseInt(selectionArgs[0]));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI - " + uri);
        }
        this.getContext().getContentResolver().notifyChange(updatedUri, null);
        return cnt;
    }
}
