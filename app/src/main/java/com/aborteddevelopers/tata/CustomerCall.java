package com.aborteddevelopers.tata;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aborteddevelopers.tata.Common.Common;
import com.aborteddevelopers.tata.Model.DataMessage;
import com.aborteddevelopers.tata.Model.FCMResponse;
import com.aborteddevelopers.tata.Model.Token;
import com.aborteddevelopers.tata.Remote.IFCMService;
import com.aborteddevelopers.tata.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime,txtAddress,txtDistance,txtCountDown;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;

    Button btnCancel,btnAccept;

    String customerId;

    IFCMService mFCMServices;

    String lat,lng;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogleAPI();
        mFCMServices = Common.getFCMService();

        //Initview

        txtAddress = (TextView)findViewById(R.id.txtAddress);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtDistance = (TextView)findViewById(R.id.txtDistance);
        txtCountDown = (TextView)findViewById(R.id.txt_count_down);

        btnAccept = (Button)findViewById(R.id.btnAccept);
        btnCancel = (Button)findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(customerId))
                      cancelBooking(customerId);

            }
        });



        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  = new Intent(CustomerCall.this,DriverTracking.class);
                //send customer location to new activity
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerId",customerId);
                startActivity(intent);
                finish();
            }
        });

        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null)
        {

             lat = getIntent().getStringExtra("lat");
             lng = getIntent().getStringExtra("lng");
            customerId = getIntent().getStringExtra("customer");

            //getdirection of welcome activity
            getDirection(lat,lng);
        }

        startTimer();

    }



    private void startTimer() {
        CountDownTimer countDownTimer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long  millisUntilFinished) {

                txtCountDown.setText(String.valueOf(millisUntilFinished/1000));

            }

            @Override
            public void onFinish() {
                if (!TextUtils.isEmpty(customerId))
                    cancelBooking(customerId);
                else
                    Toast.makeText(CustomerCall.this, "Customer Id must not be null", Toast.LENGTH_SHORT).show();

            }
        }.start();
    }

    private void cancelBooking(String customerId) {

        Token token = new Token(customerId);

       /* Notification notification = new Notification("Cancel","Driver has cancelled your request");
        Sender sender = new Sender(notification, token.getToken());*/

        Map<String,String> content= new HashMap<>();
        content.put("title","Cancel");
        content.put("message","Driver has cancelled your request");
        DataMessage dataMessage = new DataMessage(token.getToken(),content);

        mFCMServices.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if (response.body().success == 1)
                        {
                            Toast.makeText(CustomerCall.this,"Cancelled",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void getDirection(String lat, String lng) {



        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("Srinath",requestApi); // print URL for debug
            mService.getPatch(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());

                                JSONArray routes = jsonObject.getJSONArray("routes");

                                //after get routes , just get first element of element

                                JSONObject object = routes.getJSONObject(0);

                                // after get first element, we need get array with name "legs"

                                JSONArray legs = object.getJSONArray("legs");
                                //and get first elemnt of legs array

                                JSONObject legsObject = legs.getJSONObject(0);

                                // now, get distance

                                JSONObject distance = legsObject.getJSONObject("distance");

                                txtDistance.setText(distance.getString("text"));

                                //get time

                                JSONObject time = legsObject.getJSONObject("duration");

                                txtTime.setText(time.getString("text"));

                                //get address

                                String address = legsObject.getString("end_address");

                                txtAddress.setText(address);












                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                            Toast.makeText(CustomerCall.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying())
           mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying())
            mediaPlayer.start();
    }
}
