package com.aware.plugin.acpunlock;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class Algorithm extends IntentService {

    public static final String ACTION_AWARE_PLUGIN_ACP_UNLOCK_AR = "ACTION_AWARE_PLUGIN_ACP_UNLOCK_AR";
    public static final String EXTRA_DATA = "data";

    public Algorithm() {
        super(Plugin.TAG);
    }

    private DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivities) {
        DetectedActivity myActivity = null;
        int confidence = 0;
        for (DetectedActivity activity : probableActivities) {
            if (activity.getType() != DetectedActivity.RUNNING && activity.getType() != DetectedActivity.WALKING) {
                continue;
            }

            if (activity.getConfidence() > confidence) {
                myActivity = activity;
            }
        }

        return myActivity;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            DetectedActivity mostProbable = result.getMostProbableActivity();

            JSONArray activities = new JSONArray();
            List<DetectedActivity> otherActivities = result.getProbableActivities();
            for(DetectedActivity activity : otherActivities ) {
                try {
                    JSONObject item = new JSONObject();
                    item.put("activity", getActivityName(activity.getType()));
                    item.put("confidence", activity.getConfidence());
                    activities.put(item);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Log.d("UNLOCK","G48");
            int activityType = mostProbable.getType();
            if(activityType==DetectedActivity.ON_FOOT)
            {
                DetectedActivity betterActivity = walkingOrRunning(result.getProbableActivities());
                if (null != betterActivity) {
                    mostProbable = betterActivity;
                }
            }
            ActivityRecognitionService.current_confidence = mostProbable.getConfidence();
            ActivityRecognitionService.current_activity = mostProbable.getType();

            String activity_name = getActivityName(ActivityRecognitionService.current_activity);

            int confidence = ActivityRecognitionService.current_confidence;

            //Log.d("UNLOCK","G53");
            ContentValues data = new ContentValues();
            data.put(Provider.Unlock_Monitor_Data.TIMESTAMP, System.currentTimeMillis());
            data.put(Provider.Unlock_Monitor_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
            data.put(Provider.Unlock_Monitor_Data.ACTIVITY, activity_name);
            data.put(Provider.Unlock_Monitor_Data.CONFIDENCE, confidence);

            //send to AWARE

            Intent context_unlock = new Intent();
            context_unlock.setAction(ACTION_AWARE_PLUGIN_ACP_UNLOCK_AR);
            context_unlock.putExtra(EXTRA_DATA, data);
            sendBroadcast(context_unlock);

            //Log.d("UNLOCK", "G60");
            getContentResolver().insert(Provider.Unlock_Monitor_Data.CONTENT_URI, data);


            Log.d("UNLOCK", "User is: " + activity_name + " (conf:" + ActivityRecognitionService.current_confidence + ")");
        }
    }

    public static String getActivityName(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
            //actually, the following two cannot be obtained without walkingOrRunning
            case DetectedActivity.RUNNING:
                return "running";
            case DetectedActivity.WALKING:
                return "walking";
            default:
                return "unknown";
        }
    }
}