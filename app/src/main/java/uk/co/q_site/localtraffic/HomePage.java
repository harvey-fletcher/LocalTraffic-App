package uk.co.q_site.localtraffic;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.IndianCalendar;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**

    Program written by Harvey Fletcher
    14/02/2018 19:02

 **/

public class HomePage extends AppCompatActivity{

    //This is the context that we run this pge under
    public Context ctx = HomePage.this;

    //Used for scheduling
    public Timer timer = new Timer();
    public Timer TokenRefresher = new Timer();

    public String AppHost = "http://82.10.188.99/LocalTrafficApp/api_v2.php?Token=";

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

        //Ensure the token gets refreshed often enough (2 hour)
        TokenRefresher.schedule(new tokenRefresh(),0,7200000);
    }

    public void OrderTraffic(){
        //Get all the stored traffic information
        try{
            JSONArray TrafficInformation = new JSONArray();
            try {
                TrafficInformation = new JSONArray(sharedPrefs().getString("TrafficInformation", ""));
            }catch (Exception e){
                DisplayNoTraffic();
            }

            ArrayList<String> Roads = new ArrayList<String>();
            ArrayList<JSONArray> SortedTraffic = new ArrayList<JSONArray>();

            if(TrafficInformation.length() == 0){
                DisplayNoTraffic();
                return;
            }

            for(int i=0;i<TrafficInformation.length();i++){
                JSONObject info = new JSONObject(TrafficInformation.getString(i));
                String RoadName = info.getString("road");

                int has_position = 0;

                for(int r=0;r<Roads.size();r++){
                    if(Roads.get(r).matches(RoadName)){
                        has_position = 1;
                    }
                }

                if(has_position == 0){
                    Roads.add(RoadName);
                }
            }

            for(int i=0;i<Roads.size();i++){
                SortedTraffic.add(new JSONArray());
            }

            for(int i=0;i<TrafficInformation.length();i++){
                String RoadName = new JSONObject(TrafficInformation.getString(i)).getString("road");
                JSONArray CurrentEvents = SortedTraffic.get(Roads.indexOf(RoadName));
                CurrentEvents.put(new JSONObject(TrafficInformation.getString(i)));
            }

            DisplayTraffic(SortedTraffic, Roads);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void DisplayNoTraffic(){
        RelativeLayout InnerContainer = (RelativeLayout)findViewById(R.id.InnerContainer);
        InnerContainer.removeAllViews();


        TextView NoTraffic = new TextView(ctx);
        NoTraffic.setText("There is no traffic");
        NoTraffic.setTextSize(25);
        InnerContainer.addView(NoTraffic);
    }

    public void DisplayTraffic(ArrayList<JSONArray> SortedTraffic, ArrayList<String> Roads){
        RelativeLayout InnerContainer = (RelativeLayout)findViewById(R.id.InnerContainer);
        InnerContainer.removeAllViews();

        int ContainerID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        for(int i=0;i<SortedTraffic.size();i++){
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

            //Apply the roadname to the spannable
            String InformationString = Roads.get(i);

            //Loop through all the information and append it to the string
            for(int a=0;a<SortedTraffic.get(i).length(); a++){
                try{
                    //Get each traffic event
                    JSONArray Info = SortedTraffic.get(i);
                    JSONObject Details = Info.getJSONObject(a);

                    InformationString = InformationString + "\n \n" + Details.getString("category") + "\n" + Details.getString("description").replace("|","\n") + "\n \n";
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

    public class tokenRefresh extends TimerTask{

        @Override
        public void run() {
            String token = FirebaseInstanceId.getInstance().getToken();

            String url = AppHost + token;

            SyncHttpClient client = new SyncHttpClient();

            System.out.println(url);

            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    System.out.println("Token Refreshed!");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(HomePage.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                }
            });
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