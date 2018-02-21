package uk.co.q_site.localtraffic;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Harvey on 11/02/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            //Get the traffic information
            JSONArray TrafficInformationItems = new JSONArray(new JSONObject(remoteMessage.getData()).getString("traffic_information"));

            //Are we going to display an alert?
            int DisplayNotification = 0;

            //This is an array of traffic events we have already received a notification for
            JSONObject DisplayedNotifications = new JSONObject();
            try{
                DisplayedNotifications = new JSONObject(sharedprefs().getString("DisplayedNotifications",""));
            } catch (Exception e){
                e.printStackTrace();
            }

            //These are the notification preferences
            JSONObject NotificationPreferences = new JSONObject();
            try{
                NotificationPreferences = new JSONObject(sharedprefs().getString("NotificationPreferences",""));
            } catch (Exception e){
                System.out.println("Something went wrong getting NotificationPreferences");
                e.printStackTrace();
            }

            if(TrafficInformationItems.length() == 0){
                DisplayedNotifications = new JSONObject();
            }

            for(int i=0;i<TrafficInformationItems.length();i++){
                try{
                    //Get the individual traffic alert
                    JSONObject Item = new JSONObject(TrafficInformationItems.getString(i));

                    //What road is this alert for
                    String RoadName = Item.getString("road");

                    //If the road has a notification preference, choose what to do, else, assume we ARE going to display the notification, and create a shared preference for it that says we do.
                    try {
                        boolean Preference = NotificationPreferences.getBoolean(RoadName);

                        boolean AlreadyDisplayed = true;

                        try {
                            DisplayedNotifications.getString(NotificationPreferences.getString(RoadName));
                        } catch (Exception e){
                            try{
                                DisplayedNotifications.put(NotificationPreferences.getString(RoadName), true);
                                AlreadyDisplayed = false;
                            } catch (Exception E){
                                e.printStackTrace();
                            }
                        }

                        if(Preference && !AlreadyDisplayed){
                            DisplayNotification = 1;
                        }
                    } catch (Exception e){
                        DisplayNotification = 1;

                        try{
                            NotificationPreferences.put(RoadName, true);
                        } catch (Exception E){
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //If we are wanting to display a notification, do that, else, do nothing.
            if(DisplayNotification == 1){
                try{
                    //Generate a notification ID  (Used for displaying multiple notifications)
                    int mNotificationID =  (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

                    //Get an instance of the notification manager service
                    NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                    //The notification builder
                    NotificationCompat.Builder NB;

                    //Android Oreo and above require a notification channel be created.
                    if(Build.VERSION.SDK_INT >= 26){
                        String channelId = "LocalTrafficChannel";
                        CharSequence channelName = "LocalTrafficChannel";
                        int importance = NotificationManager.IMPORTANCE_LOW;
                        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                        mNotifyMgr.createNotificationChannel(notificationChannel);

                        NB = new NotificationCompat.Builder(this, "LocalTrafficChannel")
                                .setSmallIcon(R.drawable.cars_icon);

                    } else {
                        //This is used to display notifications
                        NB = new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.cars_icon);
                    }

                    //Decide what type of notification it is
                    NB.setContentTitle("Traffic Alert!");
                    NB.setContentText("Tap here to open app.");

                    Intent openActivity = new Intent(this, HomePage.class);

                    //Set up the notification so it opens the activity.
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openActivity, PendingIntent.FLAG_ONE_SHOT);
                    NB.setContentIntent(contentIntent);

                    //Build the notification and issue it
                    mNotifyMgr.notify(mNotificationID, NB.build());

                    //Light the screen up.
                    LightUpScreen();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            System.out.println("DISPLAY NOTIFICATION ===> " + DisplayNotification);
            System.out.println("Traffic Info is now: \n"+ TrafficInformationItems);

            //This is the editor that will be used to save the traffic information
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this).edit();
            editor.putString("TrafficInformation", TrafficInformationItems.toString());
            editor.putString("NotificationPreferences", NotificationPreferences.toString());
            editor.putString("DisplayedNotifications", DisplayedNotifications.toString());
            editor.commit();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public SharedPreferences sharedprefs(){
        return PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this);
    }

    public void PlayNotificationSound(){
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.chime_sound_effect);
        mediaPlayer.start();
    }

    public void LightUpScreen(){
        //The power manager object
        PowerManager pm = (PowerManager)this.getSystemService(this.POWER_SERVICE);

        //Is the screen already on?
        boolean isScreenOn = pm.isScreenOn();

        //If the screen is NOT on, light it up for 5 seconds, and then turn it off again.
        if(isScreenOn == false){
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(5000);
        }
    };
}
