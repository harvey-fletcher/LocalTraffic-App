package uk.co.q_site.localtraffic;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.Key;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Harvey on 14/02/2018.
 */

public class Settings extends AppCompatActivity{

    public Context ctx = Settings.this;
    public SharedPreferences.Editor editor;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Exit
        ImageView ExitIcon = findViewById(R.id.ExitIcon);
        ExitIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Setup the editor
        editor = sharedprefs().edit();

        //Get a list of roads
        ArrayList<String> Roads = OrderTraffic();
        DisplayPreferenceOptions(Roads);

        //The time preferences
        TimePreferences();
    }

    public void TimePreferences(){
        //First we need to know if the time preferences are enabled
        JSONObject timesPrefs = new JSONObject();
        boolean defaultState = false;
        try{
            timesPrefs = new JSONObject(sharedprefs().getString("TimePreferences",""));
            defaultState = timesPrefs.getBoolean("enabled");
        } catch (Exception e){
            try{
                timesPrefs.put("enabled", false);
                timesPrefs.put("time1A", "06:00");
                timesPrefs.put("time1B", "09:00");
                timesPrefs.put("time2A", "16:00");
                timesPrefs.put("time2B", "18:00");

                SharedPreferences.Editor editor = sharedprefs().edit();

                editor.putString("TimePreferences", timesPrefs.toString());
                editor.commit();
            } catch (Exception E){
                e.printStackTrace();
            }
        }

        //Need to declare this as a final
        final boolean defaultFinal = defaultState;

        //There's a switch which controls the state of notifications
        final Switch TimesPrefsSW = findViewById(R.id.NotificationsTimesSW);
        TimesPrefsSW.setChecked(defaultFinal);

        //This is the relative layout that displays the time boxes
        RelativeLayout Times = findViewById(R.id.TimesLayout);

        //When the switch is clicked, we need to update prefs
        TimesPrefsSW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateNotificationsTimesPreferences(defaultFinal);
            }
        });

        //Decide if we need to show the time preferences
        if(defaultFinal){
            Times.setVisibility(View.VISIBLE);
            DropDownControllersTimes(timesPrefs);
        } else {
            Times.setVisibility(View.INVISIBLE);
            return;
        }
    }

    public void DropDownControllersTimes(JSONObject savedPreferences){
        //These are the four drop downs.
        final Spinner time1A = findViewById(R.id.time1A);
        final Spinner time1B = findViewById(R.id.time1B);
        final Spinner time2A = findViewById(R.id.time2A);
        final Spinner time2B = findViewById(R.id.time2B);

        //A list of hours that must be selectable
        ArrayList<String> HoursList = new ArrayList<String>();
        HoursList.add("06:00");
        HoursList.add("07:00");
        HoursList.add("08:00");
        HoursList.add("09:00");
        HoursList.add("10:00");
        HoursList.add("11:00");
        HoursList.add("12:00");
        HoursList.add("13:00");
        HoursList.add("14:00");
        HoursList.add("15:00");
        HoursList.add("16:00");
        HoursList.add("17:00");
        HoursList.add("18:00");
        HoursList.add("19:00");
        HoursList.add("20:00");

        //Have all the lists set to the list of hours
        ArrayAdapter<String> HAdapter= new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_dropdown_item, HoursList);
        time1A.setAdapter(HAdapter);
        time1B.setAdapter(HAdapter);
        time2A.setAdapter(HAdapter);
        time2B.setAdapter(HAdapter);

        //Select the values from preferences
        try{
            time1A.setSelection(HoursList.indexOf(savedPreferences.getString("time1A")));
            time1B.setSelection(HoursList.indexOf(savedPreferences.getString("time1B")));
            time2A.setSelection(HoursList.indexOf(savedPreferences.getString("time2A")));
            time2B.setSelection(HoursList.indexOf(savedPreferences.getString("time2B")));
        } catch (Exception e){
            e.printStackTrace();
        }

        //This is the save button
        Button SaveButton = findViewById(R.id.saveButton);
        SaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveTimeChanges(time1A.getSelectedItem().toString(), time1B.getSelectedItem().toString(), time2A.getSelectedItem().toString(), time2B.getSelectedItem().toString());
            }
        });
    }

    public void SaveTimeChanges(String time1A, String time1B, String time2A, String time2B){
        try{
            JSONObject timesPrefs = new JSONObject();

            timesPrefs.put("enabled", true);
            timesPrefs.put("time1A", time1A);
            timesPrefs.put("time1B", time1B);
            timesPrefs.put("time2A", time2A);
            timesPrefs.put("time2B", time2B);

            SharedPreferences.Editor editor = sharedprefs().edit();

            System.out.println(timesPrefs);

            editor.putString("TimePreferences", timesPrefs.toString());
            editor.commit();

            Toast.makeText(ctx, "Changes saved.", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void UpdateNotificationsTimesPreferences(boolean InitialState){
        try{
            if(InitialState){
                JSONObject timesPrefs = new JSONObject();

                timesPrefs.put("enabled", false);
                timesPrefs.put("time1A", "06:00");
                timesPrefs.put("time1B", "09:00");
                timesPrefs.put("time2A", "16:00");
                timesPrefs.put("time2B", "18:00");

                SharedPreferences.Editor editor = sharedprefs().edit();

                editor.putString("TimePreferences", timesPrefs.toString());
                editor.commit();
            } else {
                JSONObject timesPrefs = new JSONObject();

                timesPrefs.put("enabled", true);
                timesPrefs.put("time1A", "06:00");
                timesPrefs.put("time1B", "09:00");
                timesPrefs.put("time2A", "16:00");
                timesPrefs.put("time2B", "18:00");

                SharedPreferences.Editor editor = sharedprefs().edit();

                editor.putString("TimePreferences", timesPrefs.toString());
                editor.commit();
            }

            TimePreferences();
        } catch (Exception e){
            Toast.makeText(ctx, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }
    }

    public void DisplayPreferenceOptions(ArrayList<String> Roads){
        //This is the inner container where the switches will go.
        RelativeLayout NPrefContainer = (RelativeLayout)findViewById(R.id.NotificationsPreferencesContainer);

        for(int i=0;i<Roads.size();i++){
            //Each road needs a container
            RelativeLayout Container = new RelativeLayout(ctx);

            //Each container will need an ID
            Container.setId(i+1);

            //If this is not the first container, it needs to go below the last one
            if(i>0){
                RelativeLayout.LayoutParams CParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                CParams.addRule(RelativeLayout.BELOW, i);
                CParams.setMargins(0,30,0,0);
                Container.setLayoutParams(CParams);
            }

            //There needs to be a switch
            Switch PrefSwitch = new Switch(ctx);
            PrefSwitch.setText(Roads.get(i));
            PrefSwitch.setTextSize(15);

            //The switch needs to be set to its correct value, try that else assume switch is one, and save it to sharedprefs
            try{
                JSONObject NotificationPreferences = new JSONObject(sharedprefs().getString("NotificationPreferences",""));
                PrefSwitch.setChecked(NotificationPreferences.getBoolean(Roads.get(i)));
            } catch (Exception e){
                try{
                    JSONObject NotificationPreferences = new JSONObject();
                    PrefSwitch.setChecked(true);
                    System.out.println(NotificationPreferences);
                    NotificationPreferences.put(Roads.get(i), true);
                    editor.putString("NotificationPreferences", NotificationPreferences.toString());
                    editor.commit();
                } catch (Exception E){
                    E.printStackTrace();
                }
            }

            //We'll need to know when the switch changes state so that we can update shared prefs.
            PrefSwitch.setOnClickListener(OnSwitchStateChange(PrefSwitch, Roads.get(i)));

            //Make everything visible
            Container.addView(PrefSwitch);
            NPrefContainer.addView(Container);
        }
    }

    View.OnClickListener OnSwitchStateChange(final Switch PrefSwitch, final String RoadName){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    JSONObject NotificationPreferences = new JSONObject(sharedprefs().getString("NotificationPreferences",""));
                    NotificationPreferences.put(RoadName, PrefSwitch.isChecked());
                    editor.putString("NotificationPreferences", NotificationPreferences.toString());
                    editor.commit();
                    Toast.makeText(ctx, "Changes saved.", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    public ArrayList<String> OrderTraffic(){
        //This will be a list of roads
        ArrayList<String> Roads = new ArrayList<String>();

        //Get all the stored traffic information
        try{
            JSONArray TrafficInformation = new JSONArray(sharedprefs().getString("TrafficInformation",""));

            for(int i=0;i<TrafficInformation.length();i++){
                JSONObject info = new JSONObject(TrafficInformation.getString(i));

                //Split the road up into a manageable name
                String road = info.getString("road");

                //These variables are used to tell if the road already exists in the array, and if so, where
                int has_position = 0;

                //Have a look to see if the road is already listed
                for(int r=0;r<Roads.size();r++){
                    if(Roads.get(r).matches(road)){
                        has_position = 1;
                        break;
                    }
                }

                //Only add the new road if it is not already in the list
                if(has_position != 1){
                    Roads.add(road);
                }
            }

            //We need to set this so that we can cancel searching if we find the preference in the list of known roads.
            int has_position = 0;

            //These are the stored notification preferences
            JSONObject NotificationPreferences = new JSONObject(sharedprefs().getString("NotificationPreferences",""));

            //Get the keys from notification preferences
            Iterator<?> Keys = NotificationPreferences.keys();

            //Loop through the keys
            while (Keys.hasNext()){
                String road = (String)Keys.next();
                //Have a look to see if the road is already listed
                for(int r=0;r<Roads.size();r++){
                    if(Roads.get(r).matches(road)){
                        has_position = 1;
                        break;
                    }
                }

                //Only add the new road if it is not already in the list
                if(has_position != 1){
                    Roads.add(road);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return Roads;
    }

    public SharedPreferences sharedprefs(){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
