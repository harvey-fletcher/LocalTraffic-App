package uk.co.q_site.localtraffic;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class HomePage extends AppCompatActivity {

    //This is the context
    public Context ctx = HomePage.this;

    //This is the sharedpreferences editor.
    public SharedPreferences sharedPrefs;
    public SharedPreferences.Editor editor;

    //Used for refreshing views
    public Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        //Load the preferences
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();

        //These are the switches
        Switch A303Switch = findViewById(R.id.A303Switch);
        Switch M3Switch = findViewById(R.id.M3Switch);
        Switch A34Switch = findViewById(R.id.A34Switch);

        //Load the switch states
        LoadSavedNotificationPrefs();

        //Set up the preference switches so they do things
        A303Switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSwitches();
            }
        });
        M3Switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSwitches();
            }
        });
        A34Switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSwitches();
            }
        });

        //Filter and display the traffic information.
        FilterAndDisplayInformation();

        //Attempt to destroy any open notifications
        DestroyNotifications();

        //Program the help button
        ImageView HelpButton = findViewById(R.id.InformationButton);
        HelpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomePage.this, Help.class);
                startActivity(intent);
            }
        });

        //Auto refresh every 10 seconds
        timer.schedule(new timedRefresh(), 0,10000);
    }

    public void DestroyNotifications(){
        NotificationManager NM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        NM.cancelAll();
    }

    public void FilterAndDisplayInformation(){
        //This is where traffic information is stored
        JSONArray TrafficInformation = new JSONArray();

        //Try to load from shared preferences
        try{
            TrafficInformation = new JSONArray(sharedPrefs.getString("TrafficInformation",""));
        } catch (Exception e){
            e.printStackTrace();
        }

        //The 3 roads
        JSONArray A303Array = new JSONArray();
        JSONArray M3Array = new JSONArray();
        JSONArray A34Array = new JSONArray();

        //Loop through all the stored traffic information
        for(int i=0;i<TrafficInformation.length();i++){
            try{
                //Get the traffic event as a JSONObject
                JSONObject TrafficEvent = new JSONObject(TrafficInformation.getString(i));

                //Decide which road array to put it into
                if(TrafficEvent.getString("1").contains("A303")){
                    A303Array.put(TrafficEvent);
                } else if(TrafficEvent.getString("1").contains("M3")){
                    M3Array.put(TrafficEvent);
                } else if(TrafficEvent.getString("1").contains("A34")){
                    A34Array.put(TrafficEvent);
                } else {
                    Toast.makeText(ctx, "Un-Identifiable road.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //Display items in the A303 Array in the A303 Scrollview container
        DisplayA303Events(A303Array);

        //Display items in the M3 Array in the M3 Scrollview container
        DisplayM3Events(M3Array);

        //Display items in the A34 Array in the A34 Scrollview container
        DisplayA34Events(A34Array);

        System.out.println(A303Array + "\n" + M3Array + "\n" + A34Array);
    }

    public void DisplayA34Events(JSONArray A34Array){
        for(int i=0;i<A34Array.length();i++){
            //Each event needs to go in a container so we can position it.
            RelativeLayout TrafficEventContainer = new RelativeLayout(ctx);
            TrafficEventContainer.setId(i + 1);

            //These are the parameters that will be used to position the box
            RelativeLayout.LayoutParams ContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            //If this IS NOT the first element, position it below the previous one
            if(i>0){
                ContainerParams.addRule(RelativeLayout.BELOW, i);
            }

            //The container needs positioning a little bit inwards
            ContainerParams.setMargins(5,0,5,0);

            //Apply the params
            TrafficEventContainer.setLayoutParams(ContainerParams);

            //This is the textview that contains the information
            TextView EventText = new TextView(ctx);

            //We're going to put the objects in a spannable string so we can set title size and body size.
            SpannableString TrafficEventSpannable = new SpannableString("");
            String TrafficEventString = new String();

            //Populate the spannablestring
            try{
                //This is the current traffic event we are processing
                JSONObject TrafficEvent = A34Array.getJSONObject(i);

                //If there is major disruption, set the red flag
                if(TrafficEvent.getString("0").contains("M")){
                    RelativeLayout A34Flag = findViewById(R.id.A34Indicator);
                    A34Flag.setBackgroundColor(Color.parseColor("#FF0000"));
                }

                //Join all the information together
                TrafficEventString+= TrafficEvent.getString("0") + "\n";
                TrafficEventString+= TrafficEvent.getString("1") + "\n";
                TrafficEventString+= TrafficEvent.getString("2") + "\n";
                TrafficEventString+= TrafficEvent.getString("3") + "\n";

                //Make the information spannable.
                TrafficEventSpannable = new SpannableString(TrafficEventString);

                //Span the spannable string
                TrafficEventSpannable.setSpan(new RelativeSizeSpan(2f),0, TrafficEvent.getString("0").length(), 0);
            } catch (Exception e){
                e.printStackTrace();
                EventText.setText("Error: " + e.getClass().getSimpleName() );
            }

            //Add the spannable string to the textview
            EventText.setText(TrafficEventSpannable);

            //Add the textview to the scrolling view container
            TrafficEventContainer.addView(EventText);

            LinearLayout A34ScrollingContainer = findViewById(R.id.A34ScrollingContainer);
            A34ScrollingContainer.removeAllViews();
            A34ScrollingContainer.addView(TrafficEventContainer);
            A34ScrollingContainer.refreshDrawableState();
        }

        //If there is nothing, tell.
        if(A34Array.length() == 0){
            TextView NoEvent = new TextView(ctx);
            NoEvent.setText("No traffic incidents :)");
            NoEvent.setX(10);
            NoEvent.setY(10);
            NoEvent.setMinHeight(50);

            LinearLayout A34ScrollingContainer = findViewById(R.id.A34ScrollingContainer);
            A34ScrollingContainer.removeAllViews();
            A34ScrollingContainer.addView(NoEvent);
            A34ScrollingContainer.refreshDrawableState();
        }
    }

    public void DisplayM3Events(JSONArray M3Array){
        for(int i=0;i<M3Array.length();i++){
            //Each event needs to go in a container so we can position it.
            RelativeLayout TrafficEventContainer = new RelativeLayout(ctx);
            TrafficEventContainer.setId(i + 1);

            //These are the parameters that will be used to position the box
            RelativeLayout.LayoutParams ContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            //If this IS NOT the first element, position it below the previous one
            if(i>0){
                ContainerParams.addRule(RelativeLayout.BELOW, i);
            }

            //The container needs positioning a little bit inwards
            ContainerParams.setMargins(5,0,5,0);

            //Apply the params
            TrafficEventContainer.setLayoutParams(ContainerParams);

            //This is the textview that contains the information
            TextView EventText = new TextView(ctx);

            //We're going to put the objects in a spannable string so we can set title size and body size.
            SpannableString TrafficEventSpannable = new SpannableString("");
            String TrafficEventString = new String();

            //Populate the spannablestring
            try{
                //This is the current traffic event we are processing
                JSONObject TrafficEvent = M3Array.getJSONObject(i);

                //If there is major disruption, set the red flag
                if(TrafficEvent.getString("0").contains("M")){
                    RelativeLayout M3Flag = findViewById(R.id.M3Indicator);
                    M3Flag.setBackgroundColor(Color.parseColor("#FF0000"));
                }

                //Join all the information together
                TrafficEventString+= TrafficEvent.getString("0") + "\n";
                TrafficEventString+= TrafficEvent.getString("1") + "\n";
                TrafficEventString+= TrafficEvent.getString("2") + "\n";
                TrafficEventString+= TrafficEvent.getString("3") + "\n";

                //Make the information spannable.
                TrafficEventSpannable = new SpannableString(TrafficEventString);

                //Span the spannable string
                TrafficEventSpannable.setSpan(new RelativeSizeSpan(2f),0, TrafficEvent.getString("0").length(), 0);
            } catch (Exception e){
                e.printStackTrace();
                EventText.setText("Error: " + e.getClass().getSimpleName() );
            }

            //Add the spannable string to the textview
            EventText.setText(TrafficEventSpannable);

            //Add the textview to the scrolling view container
            TrafficEventContainer.addView(EventText);

            LinearLayout M3ScrollingContainer = findViewById(R.id.M3ScrollingContainer);
            M3ScrollingContainer.removeAllViews();
            M3ScrollingContainer.addView(TrafficEventContainer);
            M3ScrollingContainer.refreshDrawableState();
        }

        //If there is nothing, tell.
        if(M3Array.length() == 0){
            TextView NoEvent = new TextView(ctx);
            NoEvent.setText("No traffic incidents :)");
            NoEvent.setX(10);
            NoEvent.setY(10);
            NoEvent.setMinHeight(50);

            LinearLayout M3ScrollingContainer = findViewById(R.id.M3ScrollingContainer);
            M3ScrollingContainer.removeAllViews();
            M3ScrollingContainer.addView(NoEvent);
            M3ScrollingContainer.refreshDrawableState();
        }
    }

    public void DisplayA303Events(JSONArray A303Array){
        for(int i=0;i<A303Array.length();i++){
            //Each event needs to go in a container so we can position it.
            RelativeLayout TrafficEventContainer = new RelativeLayout(ctx);
            TrafficEventContainer.setId(i + 1);

            //These are the parameters that will be used to position the box
            RelativeLayout.LayoutParams ContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            //If this IS NOT the first element, position it below the previous one
            if(i>0){
                ContainerParams.addRule(RelativeLayout.BELOW, i);
            }

            //The container needs positioning a little bit inwards
            ContainerParams.setMargins(5,0,5,0);

            //Apply the params
            TrafficEventContainer.setLayoutParams(ContainerParams);

            //This is the textview that contains the information
            TextView EventText = new TextView(ctx);

            //We're going to put the objects in a spannable string so we can set title size and body size.
            SpannableString TrafficEventSpannable = new SpannableString("");
            String TrafficEventString = new String();

            //Populate the spannablestring
            try{
                //This is the current traffic event we are processing
                JSONObject TrafficEvent = A303Array.getJSONObject(i);

                //If there is major disruption, set the red flag
                if(TrafficEvent.getString("0").contains("M")){
                    RelativeLayout A303Flag = findViewById(R.id.A303Indicator);
                    A303Flag.setBackgroundColor(Color.parseColor("#FF0000"));
                }

                //Join all the information together
                TrafficEventString+= TrafficEvent.getString("0") + "\n";
                TrafficEventString+= TrafficEvent.getString("1") + "\n";
                TrafficEventString+= TrafficEvent.getString("2") + "\n";
                TrafficEventString+= TrafficEvent.getString("3") + "\n";

                //Make the information spannable.
                TrafficEventSpannable = new SpannableString(TrafficEventString);

                //Span the spannable string
                TrafficEventSpannable.setSpan(new RelativeSizeSpan(2f),0, TrafficEvent.getString("0").length(), 0);
            } catch (Exception e){
                e.printStackTrace();
                EventText.setText("Error: " + e.getClass().getSimpleName() );
            }

            //Add the spannable string to the textview
            EventText.setText(TrafficEventSpannable);

            //Add the textview to the scrolling view container
            TrafficEventContainer.addView(EventText);

            LinearLayout A303ScrollingContainer = findViewById(R.id.A303ScrollingContainer);
            A303ScrollingContainer.removeAllViews();
            A303ScrollingContainer.addView(TrafficEventContainer);
            A303ScrollingContainer.refreshDrawableState();
        }

        //If there is nothing, tell.
        if(A303Array.length() == 0){
            TextView NoEvent = new TextView(ctx);
            NoEvent.setText("No traffic incidents :)");
            NoEvent.setX(10);
            NoEvent.setY(10);
            NoEvent.setMinHeight(50);

            LinearLayout A303ScrollingContainer = findViewById(R.id.A303ScrollingContainer);
            A303ScrollingContainer.removeAllViews();
            A303ScrollingContainer.addView(NoEvent);
            A303ScrollingContainer.refreshDrawableState();
        }
    }

    public void UpdateSwitches(){
        //These are the switches
        Switch A303Switch = findViewById(R.id.A303Switch);
        Switch M3Switch = findViewById(R.id.M3Switch);
        Switch A34Switch = findViewById(R.id.A34Switch);

        //This is the switch state
        boolean A303State = A303Switch.isChecked();
        boolean M3State = M3Switch.isChecked();
        boolean A34State = A34Switch.isChecked();

        //Build a JSONObject for the switch states
        JSONObject NotificationPreferences = new JSONObject();
        try{
            NotificationPreferences.put("A303", A303State);
            NotificationPreferences.put("M3", M3State);
            NotificationPreferences.put("A34", A34State);
        } catch (Exception e){
            e.printStackTrace();
        }

        editor.putString("NotificationPreferences", NotificationPreferences.toString());
        editor.commit();
    }

    public void LoadSavedNotificationPrefs(){
        //Load the preferences
        JSONObject NotificationPreferences = new JSONObject();
        try{
            NotificationPreferences = new JSONObject(sharedPrefs.getString("NotificationPreferences",""));
        } catch (Exception e){
            try{
                NotificationPreferences.put("A303",true);
                NotificationPreferences.put("M3",true);
                NotificationPreferences.put("A34",true);
                editor.putString("NotificationPreferences", NotificationPreferences.toString());
            } catch (Exception E){
                e.printStackTrace();
            }
        }

        //These are the switches
        Switch A303Switch = findViewById(R.id.A303Switch);
        Switch M3Switch = findViewById(R.id.M3Switch);
        Switch A34Switch = findViewById(R.id.A34Switch);

        try{
            //Apply the preferences to the switches
            A303Switch.setChecked(Boolean.valueOf(NotificationPreferences.getString("A303")));
            M3Switch.setChecked(Boolean.valueOf(NotificationPreferences.getString("M3")));
            A34Switch.setChecked(Boolean.valueOf(NotificationPreferences.getString("A34")));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public class timedRefresh extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FilterAndDisplayInformation();
                }
            });
        }
    }
}
