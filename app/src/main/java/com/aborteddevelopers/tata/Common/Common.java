package com.aborteddevelopers.tata.Common;

import android.location.Location;

import com.aborteddevelopers.tata.Model.User;
import com.aborteddevelopers.tata.Remote.FCMClient;
import com.aborteddevelopers.tata.Remote.IFCMService;
import com.aborteddevelopers.tata.Remote.IGoogleAPI;
import com.aborteddevelopers.tata.Remote.RetrofitClient;

public class Common {

    public static final int PICK_IMAGE_REQUEST = 9999 ;
    public static String currentToken= "";

    public static Location mLastLocation = null;

    public  static  final String driver_tb1 = "Drivers";
    public  static  final String user_driver_tb1 = "DriverInformation";
    public  static  final String user_rider_tb1 = "RidersInformation";
    public  static  final String pickup_request_tb1 = "PickupRequest";
    public  static  final String token_tb1 = "Tokens";
    public static User currentUser;
    public static  final String user_field = "usr";
    public static  final String pwd_field = "pwd";

    public static  final String baseURL = "https://maps.googleapis.com";
    public static  final String fcmURL = "https://fcm.googleapis.com/";

    public static  double base_fare = 45;
    private static double time_rate = 1.25;
    private static double distance_rate = 11;

    public static double formulaPrice(double km, Double min)
    {
        return (base_fare+(time_rate*min)+(distance_rate*km));
    }



    public static IGoogleAPI getGoogleAPI()
    {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService()
    {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
