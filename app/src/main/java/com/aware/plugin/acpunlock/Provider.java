package com.aware.plugin.acpunlock;


import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;


/**
 * Created by Comet on 21/01/16.
 */
public class Provider extends ContentProvider {
    public static final int DATABASE_VERSION = 8;
    /**
     * Provider authority: com.aware.plugin.ACPUnlock.provider.ACPUnlock
     */
    public static String AUTHORITY = "com.aware.plugin.acpunlock.provider.acpunlock";
    //store activity data
    private static final int UNLOCK_MONITOR = 1;
    private static final int UNLOCK_MONITOR_ID = 2;
    //store ESM mood data
    private static final int UNLOCK_MONITOR2 = 3;
    private static final int UNLOCK_MONITOR2_ID = 4;
    //store sensor data
    private static final int UNLOCK_MONITOR3 = 5;
    private static final int UNLOCK_MONITOR3_ID = 6;

    public static final String DATABASE_NAME = "acpunlock.db";

    public static final String[] DATABASE_TABLES = {
            "plugin_acpunlock","plugin_acpunlock2","plugin_acpunlock3"
    };

    public static final class Unlock_Monitor_Data implements BaseColumns {
        private Unlock_Monitor_Data(){}

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_acpunlock");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.acpunlock";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.acpunlock";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String ACTIVITY  = "activity";    //
        public static final String CONFIDENCE  = "confidence";    //
    }

    //table2 mood
    public static final class Unlock_Monitor_Data2 implements BaseColumns {
        private Unlock_Monitor_Data2(){}

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_acpunlock2");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.acpunlock2";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.acpunlock2";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String ACTIVENESS  = "activeness";    //
        public static final String PLEASURE  = "pleasure";    //
    }
    //table 3 sensors
    public static final class Unlock_Monitor_Data3 implements BaseColumns {
        private Unlock_Monitor_Data3(){}

        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/plugin_acpunlock3");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.plugin.acpunlock3";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.plugin.acpunlock3";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";
        public static final String APPLICATIONS_FOREGROUND  = "applications_foreground";    //
        public static final String APPLICATIONS_NOTIFICATIONS  = "applications_notifications";    //
        public static final String SCREEN = "screen";
        public static final String CALL_RINGING = "call_ringing";
        public static final String MESSAGE_RECEIVED = "message_received";
        public static final String LIGHT = "double_light";
        public static final String PROXIMITY = "double_proximity";
        public static final String WIFI = "wifi"; //should be nearby ones
        public static final String RECEIVED_BYTES = "double_received_bytes";
        public static final String SENT_BYTES = "double_sent_bytes";
        //let's get these first
    }

    public static final String[] TABLES_FIELDS = {
        Unlock_Monitor_Data._ID + " integer primary key autoincrement," +
        Unlock_Monitor_Data.TIMESTAMP + " real default 0," +
        Unlock_Monitor_Data.DEVICE_ID + " text default ''," +
        Unlock_Monitor_Data.ACTIVITY + " text default ''," +
        Unlock_Monitor_Data.CONFIDENCE + " integer default 0," +
        "UNIQUE("+ Unlock_Monitor_Data.TIMESTAMP+","+ Unlock_Monitor_Data.DEVICE_ID+")",

        //table2 mood
        Unlock_Monitor_Data2._ID + " integer primary key autoincrement," +
        Unlock_Monitor_Data2.TIMESTAMP + " real default 0," +
        Unlock_Monitor_Data2.DEVICE_ID + " text default ''," +
        Unlock_Monitor_Data2.ACTIVENESS + " real default 0," +
        Unlock_Monitor_Data2.PLEASURE + " real default 0," +
        "UNIQUE("+ Unlock_Monitor_Data2.TIMESTAMP+","+ Unlock_Monitor_Data2.DEVICE_ID+")",

        //table3 sensors
        Unlock_Monitor_Data3._ID + " integer primary key autoincrement," +
        Unlock_Monitor_Data3.TIMESTAMP + " real default 0," +
        Unlock_Monitor_Data3.DEVICE_ID + " text default ''," +
        Unlock_Monitor_Data3.APPLICATIONS_FOREGROUND + " text default ''," +
        Unlock_Monitor_Data3.APPLICATIONS_NOTIFICATIONS + " text default ''," +
        Unlock_Monitor_Data3.SCREEN + " text default ''," +
        Unlock_Monitor_Data3.CALL_RINGING + " text default ''," +
        Unlock_Monitor_Data3.MESSAGE_RECEIVED + " text default ''," +
        Unlock_Monitor_Data3.LIGHT + " real default -1," +
        Unlock_Monitor_Data3.PROXIMITY + " real default -1," +
        Unlock_Monitor_Data3.WIFI + " text default ''," +
        Unlock_Monitor_Data3.RECEIVED_BYTES + " real default 0," +
        Unlock_Monitor_Data3.SENT_BYTES + " real default 0," +
        "UNIQUE("+ Unlock_Monitor_Data3.TIMESTAMP+","+ Unlock_Monitor_Data3.DEVICE_ID+")"
    };

    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;
    //table2 mood
    private static HashMap<String, String> databaseMap2;
    //table3 sensor
    private static HashMap<String, String> databaseMap3;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;


    @Override
    public boolean onCreate() {
        //AUTHORITY = getContext().getPackageName() + ".provider.template";

        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], UNLOCK_MONITOR);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0] + "/#", UNLOCK_MONITOR_ID);

        //table2 mood
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[1], UNLOCK_MONITOR2);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[1] + "/#", UNLOCK_MONITOR2_ID);

        //table3 sensor
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[2], UNLOCK_MONITOR3);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[2] + "/#", UNLOCK_MONITOR3_ID);

        databaseMap = new HashMap<>();
        databaseMap.put(Unlock_Monitor_Data._ID, Unlock_Monitor_Data._ID);
        databaseMap.put(Unlock_Monitor_Data.TIMESTAMP, Unlock_Monitor_Data.TIMESTAMP);
        databaseMap.put(Unlock_Monitor_Data.DEVICE_ID, Unlock_Monitor_Data.DEVICE_ID);
        databaseMap.put(Unlock_Monitor_Data.ACTIVITY, Unlock_Monitor_Data.ACTIVITY);
        databaseMap.put(Unlock_Monitor_Data.CONFIDENCE, Unlock_Monitor_Data.CONFIDENCE);

        //table2 mood
        databaseMap2 = new HashMap<>();
        databaseMap2.put(Unlock_Monitor_Data2._ID, Unlock_Monitor_Data2._ID);
        databaseMap2.put(Unlock_Monitor_Data2.TIMESTAMP, Unlock_Monitor_Data2.TIMESTAMP);
        databaseMap2.put(Unlock_Monitor_Data2.DEVICE_ID, Unlock_Monitor_Data2.DEVICE_ID);
        databaseMap2.put(Unlock_Monitor_Data2.ACTIVENESS, Unlock_Monitor_Data2.ACTIVENESS);
        databaseMap2.put(Unlock_Monitor_Data2.PLEASURE, Unlock_Monitor_Data2.PLEASURE);

        //table3 sensor
        databaseMap3 = new HashMap<>();
        databaseMap3.put(Unlock_Monitor_Data3._ID, Unlock_Monitor_Data3._ID);
        databaseMap3.put(Unlock_Monitor_Data3.TIMESTAMP, Unlock_Monitor_Data3.TIMESTAMP);
        databaseMap3.put(Unlock_Monitor_Data3.DEVICE_ID, Unlock_Monitor_Data3.DEVICE_ID);
        databaseMap3.put(Unlock_Monitor_Data3.APPLICATIONS_FOREGROUND, Unlock_Monitor_Data3.APPLICATIONS_FOREGROUND);
        databaseMap3.put(Unlock_Monitor_Data3.APPLICATIONS_NOTIFICATIONS, Unlock_Monitor_Data3.APPLICATIONS_NOTIFICATIONS);
        databaseMap3.put(Unlock_Monitor_Data3.SCREEN, Unlock_Monitor_Data3.SCREEN);
        databaseMap3.put(Unlock_Monitor_Data3.CALL_RINGING, Unlock_Monitor_Data3.CALL_RINGING);
        databaseMap3.put(Unlock_Monitor_Data3.MESSAGE_RECEIVED, Unlock_Monitor_Data3.MESSAGE_RECEIVED);
        databaseMap3.put(Unlock_Monitor_Data3.LIGHT, Unlock_Monitor_Data3.LIGHT);
        databaseMap3.put(Unlock_Monitor_Data3.PROXIMITY, Unlock_Monitor_Data3.PROXIMITY);
        databaseMap3.put(Unlock_Monitor_Data3.WIFI, Unlock_Monitor_Data3.WIFI);
        databaseMap3.put(Unlock_Monitor_Data3.RECEIVED_BYTES, Unlock_Monitor_Data3.RECEIVED_BYTES);
        databaseMap3.put(Unlock_Monitor_Data3.SENT_BYTES, Unlock_Monitor_Data3.SENT_BYTES);
        return true;
    }


    private boolean initializeDB() {

        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            //table2 mood
            case UNLOCK_MONITOR2:
                count = database.delete(DATABASE_TABLES[1], selection, selectionArgs);
                break;
            //table3 sensor
            case UNLOCK_MONITOR3:
                count = database.delete(DATABASE_TABLES[2], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                return Unlock_Monitor_Data.CONTENT_TYPE;
            case UNLOCK_MONITOR_ID:
                return Unlock_Monitor_Data.CONTENT_ITEM_TYPE;
            //table2 mood
            case UNLOCK_MONITOR2:
                return Unlock_Monitor_Data2.CONTENT_TYPE;
            case UNLOCK_MONITOR2_ID:
                return Unlock_Monitor_Data2.CONTENT_ITEM_TYPE;
            //table3 sensor
            case UNLOCK_MONITOR3:
                return Unlock_Monitor_Data3.CONTENT_TYPE;
            case UNLOCK_MONITOR3_ID:
                return Unlock_Monitor_Data3.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if (!initializeDB()) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                long weather_id = database.insert(DATABASE_TABLES[0], Unlock_Monitor_Data.DEVICE_ID, values);

                if (weather_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            Unlock_Monitor_Data.CONTENT_URI,
                            weather_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            //table2 mood
            case UNLOCK_MONITOR2:
                long weather_id2 = database.insert(DATABASE_TABLES[1], Unlock_Monitor_Data2.DEVICE_ID, values);

                if (weather_id2 > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            Unlock_Monitor_Data2.CONTENT_URI,
                            weather_id2);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            //table 3 sensor
            case UNLOCK_MONITOR3:
                long weather_id3 = database.insert(DATABASE_TABLES[2], Unlock_Monitor_Data3.DEVICE_ID, values);

                if (weather_id3 > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            Unlock_Monitor_Data3.CONTENT_URI,
                            weather_id3);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            //table2 mood
            case UNLOCK_MONITOR2:
                qb.setTables(DATABASE_TABLES[1]);
                qb.setProjectionMap(databaseMap2);
                break;
            //table3 sensor
            case UNLOCK_MONITOR3:
                qb.setTables(DATABASE_TABLES[2]);
                qb.setProjectionMap(databaseMap3);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case UNLOCK_MONITOR:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            case UNLOCK_MONITOR2:
                count = database.update(DATABASE_TABLES[1], values, selection,
                        selectionArgs);
                break;
            case UNLOCK_MONITOR3:
                count = database.update(DATABASE_TABLES[2], values, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}