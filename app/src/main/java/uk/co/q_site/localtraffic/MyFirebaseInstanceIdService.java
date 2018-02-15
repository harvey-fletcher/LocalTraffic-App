package uk.co.q_site.localtraffic;

import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Timer;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Harvey on 11/02/2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    public String AppHost = "http://82.10.188.99/TrafficInfo/api_v2.php?Token=";

    public void onCreate(){
        String token = FirebaseInstanceId.getInstance().getToken();

        String url = AppHost + token;

        AsyncHttpClient client = new AsyncHttpClient();

        System.out.println(url);

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(MyFirebaseInstanceIdService.this, "Welcome!", Toast.LENGTH_LONG);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(MyFirebaseInstanceIdService.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
            }
        });
    }
}
