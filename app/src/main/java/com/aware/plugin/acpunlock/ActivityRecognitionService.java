package com.aware.plugin.acpunlock;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Created by Comet on 18/01/16.
 */
public class ActivityRecognitionService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static GoogleApiClient gARClient;

    public static int current_activity = -1;
    public static int current_confidence = -1;

    @Override
    public void onCreate() {
        super.onCreate();

        Intent gARIntent = new Intent(getApplicationContext(), Algorithm.class);
        PendingIntent gARPending = PendingIntent.getService(getApplicationContext(), 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        gARClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ( !is_google_services_available() ) {
            //Log.d("UNLOCK", "Google Services activity recognition not available on this device.");
            stopSelf();
        }
        else
        {
            if( gARClient != null ) {
                gARClient.connect();
                //Log.d("UNLOCK", "A53");
            }
            /*if( gARClient != null && gARClient.isConnected() ) {
                Log.d("UNLOCK", "A56");
                Intent gARIntent = new Intent(getApplicationContext(), Algorithm.class);
                PendingIntent gARPending = PendingIntent.getService(getApplicationContext(), 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gARClient, 5000, gARPending);
            }*/
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //we might get here if phone doesn't support Google Services
        //Log.d("UNLOCK", "A68");
        if ( gARClient != null && gARClient.isConnected() ) {
            Intent gARIntent = new Intent(getApplicationContext(), Algorithm.class);
            PendingIntent gARPending = PendingIntent.getService(getApplicationContext(), 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates( gARClient, gARPending );
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Log.d("UNLOCK", "Connected to Google's Activity Recognition API");
        //this.;
        //Aware.startPlugin(this, PACKAGE_NAME);
        //Log.d("UNLOCK", "A90");
        Intent gARIntent = new Intent(getApplicationContext(), Algorithm.class);
        PendingIntent gARPending = PendingIntent.getService(getApplicationContext(), 0, gARIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //do AR per 6 secs
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(gARClient, 6000, gARPending);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connection_result) {
        if( connection_result.getErrorCode() == ConnectionResult.API_UNAVAILABLE ) {
            stopSelf();
        }
        //Log.d("UNLOCK", "Error connecting to Google's activity recognition services, will try again in 5 minutes");
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.d("UNLOCK", "Error connecting to Google's activity recognition services, will try again in 5 minutes");
    }

    private boolean is_google_services_available() {
        return (ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext()));
    }
}
