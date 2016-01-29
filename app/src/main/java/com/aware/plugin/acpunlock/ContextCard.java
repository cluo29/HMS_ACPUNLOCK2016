package com.aware.plugin.acpunlock;

/**
 * Created by Comet on 21/01/16.
 */


import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aware.utils.IContextCard;


public class ContextCard implements IContextCard {


    //Empty constructor used to instantiate this card
    public ContextCard(){}

    //You may use sContext on uiChanger to do queries to databases, etc.
    private Context sContext;

    private static int counter=0;
    //Set how often your card needs to refresh if the stream is visible (in milliseconds)
    private int refresh_interval = 1 * 1000; //1s

    //Declare here all the UI elements you'll be accessing
    private View card;
    private TextView current_state;


    private Handler uiRefresher = new Handler(Looper.getMainLooper());

    private Runnable uiChanger = new Runnable() {
        @Override
        public void run() {
            //Log.d("UNLOCK", "thread runs");
            Cursor searchActivity = sContext.getContentResolver().query(Provider.Unlock_Monitor_Data.CONTENT_URI, null, null, null, Provider.Unlock_Monitor_Data.TIMESTAMP + " DESC LIMIT 1");
            if( searchActivity != null && searchActivity.moveToFirst() ) {
                String current_activity = searchActivity.getString(searchActivity.getColumnIndex(Provider.Unlock_Monitor_Data.ACTIVITY));
                int current_confidence = searchActivity.getInt(searchActivity.getColumnIndex(Provider.Unlock_Monitor_Data.CONFIDENCE));
                //Log.d("UNLOCK", "activity = " + current_activity);

                String show_card = current_activity+", conf= "+current_confidence +", "+counter;
                if( card != null ) {
                    counter++;
                    current_state.setText(show_card);


                }
            }

            if( searchActivity != null && ! searchActivity.isClosed() )
                searchActivity.close();

            uiRefresher.postDelayed(uiChanger, refresh_interval);
        }
    };

    @Override
    public View getContextCard(Context context) {
        sContext = context;

        //Load card information to memory
        LayoutInflater sInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        card = sInflater.inflate(R.layout.card, new RelativeLayout(context));

        current_state = (TextView) card.findViewById(R.id.counter);

                //Begin refresh cycle
        uiRefresher.post(uiChanger);

        //Return the card to AWARE/apps
        return card;

    }
}
