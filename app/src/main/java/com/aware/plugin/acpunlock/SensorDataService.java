package com.aware.plugin.acpunlock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Communication;
import com.aware.Light;
import com.aware.Proximity;
import com.aware.Screen;
import com.aware.WiFi;
import com.aware.providers.Applications_Provider;
import com.aware.providers.Light_Provider;
import com.aware.plugin.acpunlock.Provider.Unlock_Monitor_Data3;
import com.aware.providers.Proximity_Provider;
import com.aware.providers.WiFi_Provider;

/**
 * Created by Comet on 28/01/16. v22 44
 */
public class SensorDataService extends Service {

    public static final String ACTION_AWARE_PLUGIN_ACP_UNLOCK_SENSOR = "ACTION_AWARE_PLUGIN_ACP_UNLOCK_SENSOR";

    public static final String EXTRA_DATA = "data";


    @Override
    public void onCreate() {

        Log.d("UNLOCK", "40");
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LIGHT, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LIGHT, 200000);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_COMMUNICATION_EVENTS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_WIFI, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_WIFI, 60);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_PROXIMITY, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_PROXIMITY, 200000);

        //application data
        IntentFilter application_filter = new IntentFilter();
        application_filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND);
        application_filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS);

        //screen data
        IntentFilter screen_filter = new IntentFilter();
        screen_filter.addAction(Screen.ACTION_AWARE_SCREEN_ON);
        screen_filter.addAction(Screen.ACTION_AWARE_SCREEN_OFF);
        screen_filter.addAction(Screen.ACTION_AWARE_SCREEN_UNLOCKED);

        //communication data
        IntentFilter communication_filter = new IntentFilter();
        communication_filter.addAction(Communication.ACTION_AWARE_CALL_RINGING);
        communication_filter.addAction(Communication.ACTION_AWARE_MESSAGE_RECEIVED);

        //light data
        IntentFilter light_filter = new IntentFilter();
        light_filter.addAction(Light.ACTION_AWARE_LIGHT);

        //proximity data
        IntentFilter proximity_filter = new IntentFilter();
        proximity_filter.addAction(Proximity.ACTION_AWARE_PROXIMITY);

        //WIFI data
        IntentFilter wifi_filter = new IntentFilter();
        wifi_filter.addAction(WiFi.ACTION_AWARE_WIFI_NEW_DEVICE);

        registerReceiver(screenListener, screen_filter);
        registerReceiver(applicationListener, application_filter);
        registerReceiver(communicationListener, communication_filter);
        registerReceiver(lightListener, light_filter);
        registerReceiver(proximityListener, proximity_filter);
        registerReceiver(wifiListener, wifi_filter);

    }


    //variables of sensor data

    private static String screen_state;
    private static String application_notification;
    private static String foreground_package;
    private static String call_ringing;
    private static String message_received;
    private static double light;
    private static double proximity;
    private static String wifi;

    public static void variableReset() {
        screen_state = "";
        application_notification = "";
        foreground_package = "";
        call_ringing = "";
        message_received = "";
        light = 0;
        proximity = 0;
        wifi = "";
    }

    //context broadcast
    public static void BroadContext(Context context) {
        //Broadcast your context here
        ContentValues data = new ContentValues();
        data.put(Unlock_Monitor_Data3.TIMESTAMP, System.currentTimeMillis());
        data.put(Unlock_Monitor_Data3.DEVICE_ID, Aware.getSetting(context.getApplicationContext(), Aware_Preferences.DEVICE_ID));
        data.put(Unlock_Monitor_Data3.APPLICATIONS_FOREGROUND, foreground_package);
        data.put(Unlock_Monitor_Data3.APPLICATIONS_NOTIFICATIONS, application_notification);
        data.put(Unlock_Monitor_Data3.SCREEN, screen_state);
        data.put(Unlock_Monitor_Data3.CALL_RINGING, call_ringing);
        data.put(Unlock_Monitor_Data3.MESSAGE_RECEIVED, message_received);
        data.put(Unlock_Monitor_Data3.LIGHT, light);
        data.put(Unlock_Monitor_Data3.PROXIMITY, proximity);
        data.put(Unlock_Monitor_Data3.WIFI, wifi);

        //send to AWARE
        Intent context_unlock = new Intent();
        context_unlock.setAction(ACTION_AWARE_PLUGIN_ACP_UNLOCK_SENSOR);
        context_unlock.putExtra(EXTRA_DATA, data);
        context.sendBroadcast(context_unlock);
        Log.d("UNLOCK", "113 broadcast");

        context.getContentResolver().insert(Unlock_Monitor_Data3.CONTENT_URI, data);
    }

    /**
     * BroadcastReceiver that will receive application events from AWARE
     */
    private static ApplicationListener applicationListener = new ApplicationListener();

    public static class ApplicationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            variableReset();

            if (intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND)) {
                Cursor cursor = context.getContentResolver().query(Applications_Provider.Applications_Foreground.CONTENT_URI, null, null, null, Applications_Provider.Applications_Foreground.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    foreground_package = cursor.getString(cursor.getColumnIndex(Applications_Provider.Applications_Foreground.PACKAGE_NAME));
                    Log.d("UNLOCK","foreground_package"+ foreground_package);
                }
                if (cursor != null && !cursor.isClosed())
                {
                    cursor.close();
                }
            }
            if (intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_NOTIFICATIONS)) {
                Cursor cursor = context.getContentResolver().query(Applications_Provider.Applications_Notifications.CONTENT_URI, null, null, null, Applications_Provider.Applications_Notifications.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    application_notification = cursor.getString(cursor.getColumnIndex(Applications_Provider.Applications_Notifications.PACKAGE_NAME));
                    Log.d("UNLOCK", "application_notification"+ application_notification);
                }
                if (cursor != null && !cursor.isClosed())
                {
                    cursor.close();
                }
            }
            //sync data!
            BroadContext(context);
        }
    }

    /**
     * BroadcastReceiver that will receiver screen events from AWARE
     */
    private static ScreenListener screenListener = new ScreenListener();

    public static class ScreenListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            variableReset();
            if (intent.getAction().equals(Screen.ACTION_AWARE_SCREEN_ON)) {
                screen_state = "on";
                Log.d("UNLOCK", "screen state on");
            }
            if (intent.getAction().equals(Screen.ACTION_AWARE_SCREEN_OFF)) {
                screen_state = "off";
                Log.d("UNLOCK", "screen state off");
            }
            if (intent.getAction().equals(Screen.ACTION_AWARE_SCREEN_UNLOCKED)) {
                screen_state = "unlocked";
                Log.d("UNLOCK", "screen state unlocked");
            }
            //sync data!
            BroadContext(context);
        }
    }

    /**
     * BroadcastReceiver that will receive communication events from AWARE
     */
    private static CommunicationListener communicationListener = new CommunicationListener();

    public static class CommunicationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            variableReset();
            if (intent.getAction().equals(Communication.ACTION_AWARE_CALL_RINGING)){
                call_ringing="yes";
                Log.d("UNLOCK", "call ringing");
            }
            if (intent.getAction().equals(Communication.ACTION_AWARE_MESSAGE_RECEIVED)){
                message_received="yes";
                Log.d("UNLOCK", "message_received");
            }
            //sync data!
            BroadContext(context);
        }
    }

    //lightListener
    /**
     * BroadcastReceiver that will receive light events from AWARE
     */

    private static LightListener lightListener = new LightListener();

    public static class LightListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            variableReset();
            if (intent.getAction().equals(Light.ACTION_AWARE_LIGHT)){
                Cursor cursor = context.getContentResolver().query(Light_Provider.Light_Data.CONTENT_URI, null, null, null, Light_Provider.Light_Data.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    light = cursor.getDouble(cursor.getColumnIndex(Light_Provider.Light_Data.LIGHT_LUX));
                    Log.d("UNLOCK","light="+ light);
                }
                if (cursor != null && !cursor.isClosed())
                {
                    cursor.close();
                }
            }

            //sync data!
            BroadContext(context);
        }
    }

    /**
     * BroadcastReceiver that will receive proximity events from AWARE
     */

    private static ProximityListener proximityListener = new ProximityListener();

    public static class ProximityListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            variableReset();
            if (intent.getAction().equals(Proximity.ACTION_AWARE_PROXIMITY)){
                Cursor cursor = context.getContentResolver().query(Proximity_Provider.Proximity_Data.CONTENT_URI, null, null, null, Proximity_Provider.Proximity_Data.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    proximity = cursor.getDouble(cursor.getColumnIndex(Proximity_Provider.Proximity_Data.PROXIMITY));
                    Log.d("UNLOCK","proximity="+ proximity);
                }
                if (cursor != null && !cursor.isClosed())
                {
                    cursor.close();
                }
            }

            //sync data!
            BroadContext(context);
        }
    }

    //wifiListener
    /**
     * BroadcastReceiver that will receive wifi events from AWARE
     */

    private static WifiListener wifiListener = new WifiListener();

    public static class WifiListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            variableReset();
            if (intent.getAction().equals(WiFi.ACTION_AWARE_WIFI_NEW_DEVICE)){
                Cursor cursor = context.getContentResolver().query(WiFi_Provider.WiFi_Data.CONTENT_URI, null, null, null, WiFi_Provider.WiFi_Data.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    wifi = cursor.getString(cursor.getColumnIndex(WiFi_Provider.WiFi_Data.BSSID));
                    Log.d("UNLOCK","wifi="+ wifi);
                }
                if (cursor != null && !cursor.isClosed())
                {
                    cursor.close();
                }
            }

            //sync data!
            BroadContext(context);
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LIGHT, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_APPLICATIONS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_COMMUNICATION_EVENTS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_SCREEN, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_WIFI, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_PROXIMITY, false);
        if(screenListener != null) {
            unregisterReceiver(screenListener);
        }
        if(applicationListener != null) {
            unregisterReceiver(applicationListener);
        }
        if(communicationListener != null) {
            unregisterReceiver(communicationListener);
        }
        if(lightListener != null) {
            unregisterReceiver(lightListener);
        }
        if(proximityListener != null) {
            unregisterReceiver(proximityListener);
        }
        if(wifiListener != null) {
            unregisterReceiver(wifiListener);
        }
    }

}