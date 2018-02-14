package uk.co.q_site.localtraffic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

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
                    JSONObject NotificationPreferences = new JSONObject(sharedprefs().getString("NotificationPreferences",""));
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
                String road = info.getString("1");
                road = road.substring(0, road.indexOf(" "));

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
        } catch (Exception e){
            e.printStackTrace();
        }

        return Roads;
    }

    public SharedPreferences sharedprefs(){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
