package com.aware.plugin.acpunlock;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RatingBar;
import android.widget.TextView;
import android.net.Uri;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;
import com.aware.plugin.acpunlock.Provider.Unlock_Monitor_Data2;

import java.util.jar.Manifest;

public class Plugin extends Aware_Plugin {

    public static final String ACTION_AWARE_PLUGIN_ACP_UNLOCK_MOOD = "ACTION_AWARE_PLUGIN_ACP_UNLOCK_MOOD";

    public static final String EXTRA_DATA = "data";

    //context
    private static ContextProducer sContext;
    //ESM window
    public static AlertDialog alert;
    //ESM answer listener
    private static AnswerListener answerListener = new AnswerListener();


    private static float rating1; //ACTIVENESS
    private static float rating2; //PLEASURE

    @Override
    public void onCreate() {
        super.onCreate();

        TAG = "AWARE::"+getResources().getString(R.string.app_name);

        //listen to screen event
        IntentFilter screen_filter = new IntentFilter();
        screen_filter.addAction(Intent.ACTION_USER_PRESENT);
        //registerReceiver(screenListener, screen_filter);

        //listen to ESM answer
        IntentFilter answer_filter = new IntentFilter("ESM_answer_submitted");
        registerReceiver(answerListener, answer_filter);


        //Activate programmatically any sensors/plugins you need here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER,true);
        //NOTE: if using plugin with dashboard, you can specify the sensors you'll use there.

        //Any active plugin/sensor shares its overall context using broadcasts
        sContext = new ContextProducer() {
            @Override
            public void onContext() {
                //Broadcast your context here
                ContentValues data = new ContentValues();
                data.put(Unlock_Monitor_Data2.TIMESTAMP, System.currentTimeMillis());
                data.put(Unlock_Monitor_Data2.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                data.put(Unlock_Monitor_Data2.ACTIVENESS, rating1);
                data.put(Unlock_Monitor_Data2.PLEASURE, rating2);
                Log.d("UNLOCK", "ACTIVENESS=" + rating1);

                //send to AWARE
                Intent context_unlock = new Intent();
                context_unlock.setAction(ACTION_AWARE_PLUGIN_ACP_UNLOCK_MOOD);
                context_unlock.putExtra(EXTRA_DATA,data);
                sendBroadcast(context_unlock);

                getContentResolver().insert(Unlock_Monitor_Data2.CONTENT_URI, data);
            }
        };
        CONTEXT_PRODUCER = sContext;

        //Add permissions you need (Support for Android M) e.g.,
        //REQUIRED_PERMISSIONS.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //To sync data to the server, you'll need to set this variables from your ContentProvider
        DATABASE_TABLES = Provider.DATABASE_TABLES;
        TABLES_FIELDS = Provider.TABLES_FIELDS;
        //table 1, 2, 3
        CONTEXT_URIS = new Uri[]{ Provider.Unlock_Monitor_Data.CONTENT_URI,Provider.Unlock_Monitor_Data2.CONTENT_URI,Provider.Unlock_Monitor_Data3.CONTENT_URI   };


        /*
        if (Aware.getSetting(this, "study_id").length() == 0) {
            Intent joinStudy = new Intent(this, Aware_Preferences.StudyConfig.class);
            joinStudy.putExtra(Aware_Preferences.StudyConfig.EXTRA_JOIN_STUDY, "https://api.awareframework.com/index.php/webservice/index/596/ypTfjMMQLOxW");
            startService(joinStudy);
        }
        */
        //unlock_thread.start();
        //Activate plugin
//        Aware.startPlugin(this, "com.aware.plugin.acpunlock");
        //start the activity recognition service
        //ESM when boot or installation
        //startBootESM();

        /*

        if (Aware.getSetting(this, "study_id").length() == 0) {
            Intent joinStudy = new Intent(this, Aware_Preferences.StudyConfig.class);
            joinStudy.putExtra(Aware_Preferences.StudyConfig.EXTRA_JOIN_STUDY, "https://api.awareframework.com/index.php/webservice/index/634/0FOT21HRz8IZ");
            startService(joinStudy);
        }

        */
    }

    //screen handle
    private static ScreenListener screenListener = new ScreenListener();

    public static class ScreenListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {

                    //Log.d("UNLOCK", "SCREEN UNLOCKED");
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alert.show();
                    alert.getWindow().getAttributes();
                    View v = (View) alert.getWindow().findViewById(android.R.id.message).getParent();
                    v.setMinimumHeight(0);
                    TextView textView = (TextView) alert.findViewById(android.R.id.message);
                    textView.setTextSize(13);
                    textView.setMinimumHeight(0);

            }
        }
    }

    //ESM when boot or installation
    private void startBootESM() {

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("What is your mood now?");
            builder.setMessage("Please choose below.");

            final View layout = inflater.inflate(R.layout.question, null);
            builder.setView(layout);

            alert = builder.create();

            final RatingBar sBar1 = (RatingBar) layout.findViewById(R.id.esm1_likert1);
            final RatingBar sBar2 = (RatingBar) layout.findViewById(R.id.esm1_likert2);

            RatingBar.OnRatingBarChangeListener barChangeListener = new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar rBar, float fRating, boolean fromUser) {
                    float rating1_unstable = sBar1.getRating();
                    float rating2_unstable = sBar2.getRating();
                    if (rating1_unstable > 0.1 && rating2_unstable > 0.1) {
                        rating1 = rating1_unstable;
                        rating2 = rating2_unstable;
                        Log.d("UNLOCK", "answer submit");
                        Log.d("UNLOCK", "answer 1=" + rating1);
                        Log.d("UNLOCK", "answer 2=" + rating2);

                        sendBroadcast(new Intent("ESM_answer_submitted"));
                        sBar1.setRating(0);
                        sBar2.setRating(0);
                        alert.dismiss();
                    }

                }
            };

            sBar1.setOnRatingBarChangeListener(barChangeListener);
            sBar2.setOnRatingBarChangeListener(barChangeListener);

            alert.setCanceledOnTouchOutside(false);

            alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alert.show();
            alert.getWindow().getAttributes();
            View v = (View) alert.getWindow().findViewById(android.R.id.message).getParent();
            v.setMinimumHeight(0);
            TextView textView = (TextView) alert.findViewById(android.R.id.message);
            textView.setTextSize(13);
            textView.setMinimumHeight(0);

    }

    //ESM answer listener
    public static class AnswerListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("ESM_answer_submitted")) {
                // ESM answer obtained

                Log.d("UNLOCK", "answer receive ACTIVENESS="+rating1);


                //Share context
                sContext.onContext();

            }

        }
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //google activity recognition service

        startService(new Intent(Plugin.this, ActivityRecognitionService.class));

        //sensor data collection service

        startService(new Intent(Plugin.this, SensorDataService.class));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Deactivate any sensors/plugins you activated here
        //e.g., Aware.setSetting(this, Aware_Preferences.STATUS_ACCELEROMETER, false);
        if(screenListener != null)
        {
            unregisterReceiver(screenListener);
        }
        if(answerListener != null)
        {
            unregisterReceiver(answerListener);
        }
        //Stop plugin
        Aware.stopPlugin(this, "com.aware.plugin.acpunlock");
    }
}
