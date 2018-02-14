package uk.co.q_site.localtraffic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.IndianCalendar;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**

    Program written by Harvey Fletcher
    14/02/2018 19:02

 **/

public class HomePage extends AppCompatActivity{

    //This is the context that we run this pge under
    public Context ctx = HomePage.this;

    //Used for scheduling
    public Timer timer = new Timer();

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        ImageView SettingsIcon = (ImageView)findViewById(R.id.SettingsButton);
        SettingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePage.this, Settings.class);
                startActivity(intent);
            }
        });

        //Make sure we stay up to date with the cache
        timer.schedule(new timedTask(),0,10000);
    }

    public void OrderTraffic(){
        //Get all the stored traffic information
        try{
            JSONArray TrafficInformation = new JSONArray(sharedPrefs().getString("TrafficInformation",""));
            System.out.println(TrafficInformation);
            ArrayList<String> Roads = new ArrayList<String>();
            ArrayList<JSONArray> SortedTraffic = new ArrayList<JSONArray>();

            for(int i=0;i<TrafficInformation.length();i++){
                JSONObject info = new JSONObject(TrafficInformation.getString(i));

                //Split the road up into a manageable name
                String road = info.getString("1");
                road = road.substring(0, road.indexOf(" "));

                //These variables are used to tell if the road already exists in the array, and if so, where
                int has_position = 0;
                int position = 0;

                //Have a look to see if the road is already listed
                for(int r=0;r<SortedTraffic.size();r++){
                    if(Roads.get(r).matches(road)){
                        has_position = 1;
                        position = r;
                        break;
                    }
                }

                //Add the new information in the right places.
                if(has_position == 1){
                    JSONArray ExistingEvents = SortedTraffic.get(position);
                    ExistingEvents.put(info);
                    SortedTraffic.add(position, ExistingEvents);
                } else {
                    JSONArray ExistingEvents = new JSONArray();
                    ExistingEvents.put(info);
                    Roads.add(road);
                    SortedTraffic.add(ExistingEvents);
                }
            }

            //Display the traffic events
            System.out.println(Roads + "\n" + SortedTraffic);
            DisplayRoads(Roads, SortedTraffic);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void DisplayRoads(ArrayList<String> Roads, ArrayList<JSONArray> Information){
        RelativeLayout InnerContainer = (RelativeLayout)findViewById(R.id.InnerContainer);
        InnerContainer.removeAllViews();

        int ContainerID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        for(int i=0;i<Roads.size();i++){
            //Create a relativelayout to put this information in
            RelativeLayout IndiContainer = new RelativeLayout(ctx);

            //We need to give it a layout so we can position it
            RelativeLayout.LayoutParams ICParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            //If this isnt the first layout, pos it below the prev one
            if(i>0){
                ICParams.addRule(RelativeLayout.BELOW, ContainerID - 1);
            }

            //Apply the params
            IndiContainer.setLayoutParams(ICParams);

            //Give this container an ID
            IndiContainer.setId(ContainerID);

            //This is the road
            String RoadName = Roads.get(i);

            //Apply the roadname to the spannable
            String InformationString = RoadName + "\n\n";

            //Loop through all the information and append it to the string
            for(int a=0;a<Information.get(i).length(); a++){
                try{
                    //Get each traffic event
                    JSONArray Info = Information.get(i);
                    JSONObject Details = Info.getJSONObject(a);

                    InformationString = InformationString + "   " + Details.getString("0").replace("-","\n  ") + "\n";
                    InformationString = InformationString + "   " + Details.getString("1") + "\n";
                    InformationString = InformationString + "   " + Details.getString("2") + "\n";
                    InformationString = InformationString + "   " + Details.getString("3") + "\n \n \n";
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            //Make the information string spannable
            SpannableString SpanInformation = new SpannableString(InformationString);
            SpanInformation.setSpan(new RelativeSizeSpan(2f), 0, InformationString.indexOf("\n"), 0);
            SpanInformation.setSpan(new RelativeSizeSpan(1.2f), InformationString.indexOf("\n"), InformationString.length(), 0);

            //The information string can go in this text box
            TextView ISText = new TextView(ctx);
            ISText.setText(SpanInformation);
            IndiContainer.addView(ISText);

            //Increment the container ID
            ContainerID++;

            //Put that in the inner container so it is visible
            InnerContainer.addView(IndiContainer);
        }
    }

    public class timedTask extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    OrderTraffic();
                }
            });
        }
    }

    public SharedPreferences sharedPrefs(){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}