package com.aware.plugin.acpunlock;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Communication;
import com.aware.Light;
import com.aware.Locations;
import com.aware.Network;
import com.aware.Proximity;
import com.aware.Screen;
import com.aware.Traffic;
import com.aware.WiFi;
import com.aware.providers.Applications_Provider;
import com.aware.providers.Light_Provider;
import com.aware.plugin.acpunlock.Provider.Unlock_Monitor_Data3;
import com.aware.providers.Proximity_Provider;
import com.aware.providers.WiFi_Provider;
import com.aware.providers.Traffic_Provider;
import com.aware.providers.Traffic_Provider.Traffic_Data;
import com.aware.providers.Locations_Provider.Locations_Data;

/**
 * Created by Comet on 28/01/16. v01291447
 */
public class SensorDataService extends Service implements SensorEventListener {

    public static final String ACTION_AWARE_PLUGIN_ACP_UNLOCK_SENSOR = "ACTION_AWARE_PLUGIN_ACP_UNLOCK_SENSOR";

    public static final String EXTRA_DATA = "data";

    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    public void onCreate() {

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

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
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_NETWORK_TRAFFIC, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_NETWORK_TRAFFIC, 60);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_NETWORK, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LOCATION_GPS, 180);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.FREQUENCY_LOCATION_NETWORK, 300);

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

        //network data
        IntentFilter network_filter = new IntentFilter();
        wifi_filter.addAction(Traffic.ACTION_AWARE_NETWORK_TRAFFIC);

        //Location data
        IntentFilter location_filter = new IntentFilter();
        wifi_filter.addAction(Locations.ACTION_AWARE_LOCATIONS);

        registerReceiver(screenListener, screen_filter);
        registerReceiver(applicationListener, application_filter);
        registerReceiver(communicationListener, communication_filter);
        registerReceiver(lightListener, light_filter);
        registerReceiver(proximityListener, proximity_filter);
        registerReceiver(wifiListener, wifi_filter);
        registerReceiver(networkListener, network_filter);
        registerReceiver(locationListener, location_filter);
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
    private static int network_type;
    private static long received_bytes;
    private static long sent_bytes;
    private static double latitude;
    private static double longitude;
    private static String locationSource;

    private static int step = 0;

    private static int light_counter=0;

    public static void variableReset() {
        screen_state = "";
        application_notification = "";
        foreground_package = "";
        call_ringing = "";
        message_received = "";
        light = -1;
        proximity = -1;
        wifi = "";
        network_type = 0; //1 = mobile 2 = wifi
        received_bytes = 0;
        sent_bytes = 0;
        latitude = -1;
        longitude = -1;
        locationSource = "";
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
        data.put(Unlock_Monitor_Data3.NETWORK_TYPE, network_type);
        data.put(Unlock_Monitor_Data3.RECEIVED_BYTES, received_bytes);
        data.put(Unlock_Monitor_Data3.SENT_BYTES, sent_bytes);
        data.put(Unlock_Monitor_Data3.LATITUDE, latitude);
        data.put(Unlock_Monitor_Data3.LONGITUDE, longitude);
        data.put(Unlock_Monitor_Data3.LOCATIONSOURCE, locationSource);
        data.put(Unlock_Monitor_Data3.STEP,step);

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
            //record light per 25 samples, each sample = 200ms
            light_counter++;
            if(light_counter<25)
            {
                return;
            }

            //reset variables
            variableReset();
            if (intent.getAction().equals(Light.ACTION_AWARE_LIGHT)){

                ContentValues light_data = intent.getParcelableExtra(Light.EXTRA_DATA);
                if (light_data != null) {
                    Log.d("UNLOCK", "Light sensor AVAILABLE");
                    light = light_data.getAsDouble("double_light_lux");
                    Log.d("UNLOCK","light="+ light);
                } else {
                    Log.d("UNLOCK", "Light sensor UNAVAILABLE");
                    light = 0;
                }
            }

            //sync data!
            BroadContext(context);
            light_counter=0;
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
                Log.d("UNLOCK", "Received proximity data");
                ContentValues proximity_data = intent.getParcelableExtra(Proximity.EXTRA_DATA);
                if (proximity_data != null) {
                    proximity = proximity_data.getAsDouble("double_proximity");
                    Log.d("UNLOCK", "proximity = "+proximity);
                } else {
                    proximity = -1;
                    Log.d("UNLOCK", "Proximity sensor UNAVAILABLE");
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

    //networkListener
    /**
     * BroadcastReceiver that will receive network events from AWARE
     */
    private static NetworkListener networkListener = new NetworkListener();

    public static class NetworkListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            variableReset();
            if (intent.getAction().equals(Traffic.ACTION_AWARE_NETWORK_TRAFFIC)){
                Log.d("UNLOCK","ACTION_AWARE_NETWORK_TRAFFIC");
                //get data!!!
                Cursor cursor = context.getContentResolver().query(Traffic_Data.CONTENT_URI, null, null, null, Traffic_Data.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    network_type = cursor.getInt(cursor.getColumnIndex(Traffic_Data.NETWORK_TYPE));
                    received_bytes = cursor.getLong(cursor.getColumnIndex(Traffic_Data.RECEIVED_BYTES));
                    sent_bytes = cursor.getLong(cursor.getColumnIndex(Traffic_Data.SENT_BYTES));
                    Log.d("UNLOCK","received_bytes="+ received_bytes);
                    Log.d("UNLOCK","sent_bytes="+ sent_bytes);
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

    //locationListener
    /**
     * BroadcastReceiver that will receive location events from AWARE
     */
    private static LocationListener locationListener = new LocationListener();

    public static class LocationListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //reset variables
            variableReset();
            if (intent.getAction().equals(Locations.ACTION_AWARE_LOCATIONS)){
                Log.d("UNLOCK","Locations");
                //get data!!!
                Cursor cursor = context.getContentResolver().query(Locations_Data.CONTENT_URI, null, null, null, Locations_Data.TIMESTAMP + " DESC LIMIT 1");
                if (cursor != null && cursor.moveToFirst()) {
                    latitude = cursor.getDouble(cursor.getColumnIndex(Locations_Data.LATITUDE));
                    longitude = cursor.getDouble(cursor.getColumnIndex(Locations_Data.LONGITUDE));
                    locationSource = cursor.getString(cursor.getColumnIndex(Locations_Data.PROVIDER));
                    Log.d("UNLOCK","latitude="+ latitude);
                    Log.d("UNLOCK","longitude="+ longitude);
                    Log.d("UNLOCK","locationSource="+ locationSource);
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

    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub
    }

    //step sensor
    //if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
        //return Math.round(event.values[0]);
    //}
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            step = Math.round(event.values[0]);
            Log.d("UNLOCK","step = "+ step);
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
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_NETWORK_TRAFFIC, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_GPS, false);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_NETWORK, false);
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
        if(networkListener != null) {
            unregisterReceiver(networkListener);
        }
        if(locationListener != null) {
            unregisterReceiver(locationListener);
        }
    }

}