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
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Harvey on 11/02/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public void onMessageReceived(RemoteMessage remoteMessage) {
        //This is the shared preferences editor
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this).edit();

        //These are the current shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(MyFirebaseMessagingService.this);

        try{
            //First of all, get the data from the message
            JSONObject MessageData = new JSONObject(remoteMessage.getData());

            //This is the traffic information we want to save
            JSONArray TrafficInformation = new JSONArray(MessageData.getString("traffic_information"));

            //Generate a notification ID  (Used for displaying multiple notifications)
            int mNotificationID =  (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            //Get an instance of the notification manager service
            NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            //The notification builder
            NotificationCompat.Builder NB;

            //Android Oreo and above require a notification channel be created.
            if(Build.VERSION.SDK_INT >= 26){
                String channelId = "local_traffic_app_channel";
                CharSequence channelName = "LocalTrafficApp";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                mNotifyMgr.createNotificationChannel(notificationChannel);

                NB = new NotificationCompat.Builder(this, "local_traffic_app_channel")
                        .setSmallIcon(R.drawable.cars_icon);

            } else {
                //This is used to display notifications
                NB = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.cars_icon);
            }

            //Set the notification content
            NB.setContentTitle("Traffic alert!");
            NB.setContentText("Tap here to open app.");

            //Set up the notification so it opens the activity.
            Intent openActivity = new Intent(MyFirebaseMessagingService.this, HomePage.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openActivity, PendingIntent.FLAG_ONE_SHOT);
            NB.setContentIntent(contentIntent);

            //We only need to inform the user if there is at least 1 event else just do nothing.
            if(TrafficInformation.length() > 0){
                //Get the user's notification preferences so we can selectively display a notification
                JSONObject NotificationPreferences;
                try{
                    NotificationPreferences = new JSONObject(sharedPrefs.getString("NotificationPreferences",""));
                } catch (Exception e){
                    NotificationPreferences = new JSONObject();
                }

                //Do we want to display a notification?
                int DisplayNotification = 0;

                try{
                    for(int t=0;t<TrafficInformation.length();t++){
                        JSONObject Event = TrafficInformation.getJSONObject(t);
                        String RoadName = Event.getString("1").substring(0, Event.getString("1").indexOf(" "));


                        for(int p=0;p<NotificationPreferences.length();p++){
                            try{
                                boolean preference = NotificationPreferences.getBoolean(RoadName);
                                if(!preference){
                                    if(DisplayNotification != 1){
                                        DisplayNotification = 0;
                                    }
                                } else {
                                    DisplayNotification = 1;
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                                DisplayNotification = 1;
                            }
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

                if(DisplayNotification == 1){
                    //Issue the notification
                    mNotifyMgr.notify(mNotificationID, NB.build());

                    //Play the notification sound
                    PlayNotificationSound();

                    //Light up the screen
                    LightUpScreen();
                }
            } else {
                TrafficInformation = new JSONArray();
            }

            //Update stored traffic information
            editor.putString("TrafficInformation", TrafficInformation.toString());
            editor.commit();

            System.out.println("These are the shared preferences \n" + sharedPrefs.getString("notifications", "") + "\n" + sharedPrefs.getString("TrafficInformation",""));
        } catch (Exception e){
            e.printStackTrace();
        }
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
