package com.aborteddevelopers.tata.Service;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.aborteddevelopers.tata.Common.Common;
import com.aborteddevelopers.tata.CustomerCall;
import com.aborteddevelopers.tata.Model.Token;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {



    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        updateTokenToServer(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null) {

            //Because i will send the firebase message with contain  lat and lng from rider app
            // So i need convert message to  latlng
            Map<String,String> data= remoteMessage.getData(); // get data from notification
            String customer = data.get("customer");
            String  lat = data.get("lat");
            String  lng = data.get("lng");

         /*   LatLng customer_location = new Gson().fromJson(message, LatLng.class);*/

            Intent intent = new Intent(getBaseContext(), CustomerCall.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("customer", customer);

            startActivity(intent);
        }
    }

    private void updateTokenToServer(String refreshedToken)
    {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tb1);


        Token token = new Token(refreshedToken);

        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
            .setValue(token);
    }
}
