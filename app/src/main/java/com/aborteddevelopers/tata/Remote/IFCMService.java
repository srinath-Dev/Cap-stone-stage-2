package com.aborteddevelopers.tata.Remote;

import com.aborteddevelopers.tata.Model.DataMessage;
import com.aborteddevelopers.tata.Model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAn812tvM:APA91bGzK0plvqHz3v0KQgU8Tsu8yuh_ZuIEZo8IQfNLt4KskAwH5OaL_yp8mPjYA3l6iMiOwBSYqwHIirGQTu3lJzByUkKfZtsC0OM87T5NiQPMPj-oq-oOIC4gxcmkYKbINP9LElOH"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}
