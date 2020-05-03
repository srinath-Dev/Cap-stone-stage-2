package com.aborteddevelopers.tata;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.aborteddevelopers.tata.Common.Common;
import com.aborteddevelopers.tata.Model.Token;
import com.aborteddevelopers.tata.Model.User;
import com.aborteddevelopers.tata.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.aborteddevelopers.tata.Common.Common.mLastLocation;

public class DriverHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback {

    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallback;


    private static final  int MY_PERMISSION_REQUEST_CODE = 7000;
    private static  final int PLAY_SERVICE_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private  GoogleApiClient mGoogleApiClient;


    private static int UPDATE_INTERVAL=5000;
    private static int FATEST_INTERVAL=3000;
    private static int DISPLACEMENT=10;
    SupportMapFragment mapFragment;

    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private  double lat,lng;
    private Handler handler;
    private LatLng startPosition , endPosition, currentPosition;
    private int index,next;
    //private Button btnGo;

    private String destination;
    private PolylineOptions polylineOptions;
    private PolylineOptions blackPolylineOptions;
    private Polyline blackPolyline,greyPolyline;
    private IGoogleAPI mService;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;




    DatabaseReference onlineRef, currentUserRef;

    Runnable drawPathRunnable = new Runnable() {
        @Override
        public void run() {
            if (index<polyLineList.size()-1)
            {

                index++;
                next = index+1;
            }
            if (index < polyLineList.size()-1)
            {
                startPosition = polyLineList.get(index);
                endPosition = polyLineList.get(next);
            }

            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    v = valueAnimator.getAnimatedFraction();
                    lng = v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat = v*endPosition.latitude+(1-v)*startPosition.latitude;
                    LatLng newPos = new LatLng(lat,lng);
                    carMarker.setPosition(newPos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()
                    ));
                }
            });
            valueAnimator.start();
            handler.postDelayed(this,3000);
        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude< endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+270);

        return -1;
    }

    DatabaseReference ref;
    GeoFire geoFire;
    Marker mCurrent;
    MaterialAnimatedSwitch location_switch;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);




        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_trip_history, R.id.nav_way_bill,R.id.nav_help,R.id.nav_settings,R.id.nav_sign_out)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        setupDrawerContent(navigationView);

        View navigationHeaderView = navigationView.getHeaderView(0);
        TextView txtName = (TextView)navigationHeaderView.findViewById(R.id.txtDriverName);
        TextView  txtStars = (TextView)navigationHeaderView.findViewById(R.id.txtStars);
        CircleImageView imageAvatar = (CircleImageView)navigationHeaderView.findViewById(R.id.image_avatar);

        txtName.setText(Common.currentUser.getName().toString());
        txtStars.setText(Common.currentUser.getRates());

        if (Common.currentUser.getAvatarUrl()!=null && !TextUtils.isEmpty(Common.currentUser.getAvatarUrl())) {
            Picasso.with(this)
                    .load(Common.currentUser.getAvatarUrl())
                    .into(imageAvatar);
        }



        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapp);
        mapFragment.getMapAsync(this);




        location_switch=findViewById(R.id.location_switchh);
        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {
                    FirebaseDatabase.getInstance().goOnline(); //

                    buildLocationCallBack();
                    buildLocationRequest();

                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());

                    //geofire
                    ref= FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(Common.currentUser.getCarType());
                    geoFire=new GeoFire(ref);

                    displayLocation();
                    Snackbar.make(mapFragment.getView(), "You r online", Snackbar.LENGTH_SHORT)
                            .show();
                }
                else{

                    FirebaseDatabase.getInstance().goOffline(); // set disconnect

                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);


                    mCurrent.remove();

                    mMap.clear();
                    if (handler != null)
                        handler.removeCallbacks(drawPathRunnable);
                    Snackbar.make(mapFragment.getView(), "You r offline bro", Snackbar.LENGTH_SHORT)
                            .show();


                }
            }
        });

        polyLineList = new ArrayList<>();

        String apiKey = getString(R.string.google_api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

// Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                if (location_switch.isChecked())
                {
                    destination = place.getName()+ ", " + place.getId().toString();
                    destination = destination.replace(" ","+");

                    getDirection();
                }

                else
                {
                    Toast.makeText(DriverHome.this,"Please change your status to online",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(DriverHome.this,""+status.toString(),Toast.LENGTH_SHORT).show();
            }
        });




        //placesApi








        setUpLocation();

        mService  = Common.getGoogleAPI();

        updateFirebaseToken();
    }

    @Override
    protected void onResume() {

        super.onResume();

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tb1)
                .child(Common.currentUser.getCarType())
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //we will remove value from driver tb1 when driver  disconnected

                currentUserRef.onDisconnect().removeValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onDestroy() {

        FirebaseDatabase.getInstance().goOffline(); // set disconnect

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);


        mCurrent.remove();

        mMap.clear();
        if (handler != null)
            handler.removeCallbacks(drawPathRunnable);

        super.onDestroy();
    }

    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.token_tb1);


        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void getDirection() {

        currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi = null;
        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("Srinath",requestApi); // print URL for debug
            mService.getPatch(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i=0;i<jsonArray.length();i++)
                                {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polyLineList = decodePoly(polyline);
                                }

                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (LatLng latLng:polyLineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.GRAY);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);

                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size()-1))
                                        .title("Pickup Location"));

                                //Animation

                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                        List<LatLng> points = greyPolyline.getPoints();
                                        int percentValue = (int)valueAnimator.getAnimatedValue();
                                        int size = points.size();
                                        int newPoints = (int) (size * (percentValue/100.0f));
                                        List<LatLng> p = points.subList(0,newPoints);
                                        blackPolyline.setPoints(p);
                                    }
                                });
                                polyLineAnimator.start();

                                carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.redcar)));

                                handler = new Handler();
                                index = -1;
                                next = 1;
                                handler.postDelayed(drawPathRunnable,3000);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                            Toast.makeText(DriverHome.this,""+t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                   buildLocationCallBack();
                   buildLocationRequest();
                        if(location_switch.isChecked())
                            displayLocation();


                }
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else{

                buildLocationRequest();
                buildLocationCallBack();
                if(location_switch.isChecked()) {
                    ref = FirebaseDatabase.getInstance().getReference(Common.driver_tb1).child(Common.currentUser.getCarType());
                    geoFire = new GeoFire(ref);

                    displayLocation();
                }

        }
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {

                for (Location location:locationResult.getLocations())
                {
                    mLastLocation = location;

                }
                displayLocation();

            }
        };
    }

    private void buildLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        mLastLocation = location;

                        if (mLastLocation != null){
                            if (location_switch.isChecked()) {
                                String driverid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                                final double lat = mLastLocation.getLatitude();
                                final double log = mLastLocation.getLongitude();

                                geoFire.setLocation(driverid, new GeoLocation(lat, log), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //Marker
                                        if (mCurrent != null)
                                            mCurrent.remove();

                                        mCurrent=mMap.addMarker(new MarkerOptions().position(new LatLng(lat, log))
                                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker))
                                                .title("Your location"));
                                        //camera
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, log), 15.0f));

                                        //rotation markerr


                                    }
                                });
                            }
                        }
                        else{
                            Log.d("ERROR","Cannot get you location");
                        }

                    }
                });



    }
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }






    private void rotationMarker(final Marker mCurrent, final float i, GoogleMap mMap) {
        final Handler handler=new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation =mCurrent.getRotation();
        final long duration=1500;

        final Interpolator interpolator=new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsedd=SystemClock.uptimeMillis()-start;
                float t=interpolator.getInterpolation((float)elapsedd/duration);
                float rot=t*i+(1-t)*startRotation;
                mCurrent.setRotation(-rot>180?rot/2:rot);
                if(t<1.0){
                    handler.postDelayed(this,16);
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }








    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean isSuccess = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this,R.raw.my_map_style)
            );

            if (!isSuccess)
                Log.e("ERROR","Map style load failed");
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //update location

        buildLocationRequest();
        buildLocationCallBack();

        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.nav_trip_history:

                break;

            case R.id.nav_car_type:

                showDialogUpdateCarType();

                break;

            case R.id.nav_way_bill:


                break;

            case R.id.nav_update_info:
                showDialogUpdateInfo();


                break;
            case R.id.nav_change_pwd:
                showDialogChangePwd();

                break;
            case R.id.nav_help:

                break;
            case R.id.nav_settings:

                break;
            case R.id.nav_sign_out:
                signOut();

                break;
            default:

        }


    }

    private void showDialogUpdateCarType() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DriverHome.this);
        alertDialog.setTitle("UPDATE VEHICLE TYPE");
        alertDialog.setMessage("Please Select Your Car Type ");

        LayoutInflater inflater = this.getLayoutInflater();
        View carType = inflater.inflate(R.layout.layout_update_car_type,null);

        final RadioButton tatax = (RadioButton)carType.findViewById(R.id.rdi_tatax);
        final RadioButton tataxl = (RadioButton)carType.findViewById(R.id.rdi_tataxl);

        if (Common.currentUser.getCarType().equals("TATA X"))
            tatax.setChecked(true);
        else if (Common.currentUser.getCarType().equals("TATA XL"))
            tataxl.setChecked(true);

        alertDialog.setView(carType);

        //set button

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(DriverHome.this).build();
                waitingDialog.show();



                Map<String,Object> updateInfo = new HashMap<>();
                if (tatax.isChecked())
                    updateInfo.put("carType",tatax.getText().toString());
                else if (tataxl.isChecked())
                    updateInfo.put("carType",tataxl.getText().toString());

                DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);
                driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){

                                    currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tb1)
                                            .child(Common.currentUser.getCarType())
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    Toast.makeText(DriverHome.this, "Vehicle type Updated !", Toast.LENGTH_SHORT).show();
                                }

                                else
                                    Toast.makeText(DriverHome.this, "Vehicle type Update Failed", Toast.LENGTH_SHORT).show();

                                waitingDialog.dismiss();
                            }
                        });

                //refresh driver data
                driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Common.currentUser = dataSnapshot.getValue(User.class);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDialog.show();
    }

    private void showDialogUpdateInfo() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DriverHome.this);
        alertDialog.setTitle("UPDATE INFORMATION");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName = (MaterialEditText)layout_pwd.findViewById(R.id.edtNamee);
        final MaterialEditText edtPhone = (MaterialEditText)layout_pwd.findViewById(R.id.edtPhonee);
        final ImageView image_upload = (ImageView)layout_pwd.findViewById(R.id.imageUpload);
        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        alertDialog.setView(layout_pwd);

        //set button

        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(DriverHome.this).build();
                waitingDialog.show();

                String name = edtName.getText().toString();
                String phone = edtPhone.getText().toString();

                Map<String,Object> updateInfo = new HashMap<>();
                if (!TextUtils.isEmpty(name))
                    updateInfo.put("name",name);
                if (!TextUtils.isEmpty(phone))
                    updateInfo.put("phone",phone);

                DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);
                driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(DriverHome.this,"Information Updated !",Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(DriverHome.this, "Information Update Failed", Toast.LENGTH_SHORT).show();

                                waitingDialog.dismiss();
                            }
                        });


            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDialog.show();


    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode ==RESULT_OK
            && data != null && data.getData() != null)
        {
            Uri saveUri = data.getData();
            if (saveUri != null)
            {
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading");
                mDialog.show();

                String imageName = UUID.randomUUID().toString(); // random name image upload
                final StorageReference imageFolder = storageReference.child("images/"+imageName);
                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                mDialog.dismiss();

                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        //update this url to avatar property of user
                                        Map<String,Object> avatarUpdate = new HashMap<>();
                                        avatarUpdate.put("avatarUrl",uri.toString());

                                        DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);
                                        driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .updateChildren(avatarUpdate)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                            Toast.makeText(DriverHome.this,"Uploaded !",Toast.LENGTH_SHORT).show();
                                                        else
                                                            Toast.makeText(DriverHome.this, "Upload Error", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }
                                });

                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0* taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                mDialog.setMessage("Uploaded"+progress+"%");
                            }
                        });

            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id==R.id.nav_trip_history)
        {

        }else if (id == R.id.nav_way_bill)
        {

        }else if (id == R.id.nav_change_pwd)
        {
            showDialogChangePwd();



        }else if (id == R.id.nav_help)
        {

        }else if (id == R.id.nav_settings)
        {

        }else if (id == R.id.nav_sign_out)
        {
            signOut();

        }

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialogChangePwd() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DriverHome.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd,null);

        final MaterialEditText edtPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = (MaterialEditText)layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final android.app.AlertDialog waitingDialog = new SpotsDialog.Builder().setContext(DriverHome.this).build();
                waitingDialog.show();

                if (edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    //get auth create from user for re auth

                    AuthCredential credential = EmailAuthProvider.getCredential(email,edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            //update driver information password column
                                                            Map<String,Object> password = new HashMap<>();

                                                            password.put("password",edtRepeatPassword.getText().toString());
                                                            DatabaseReference driverInformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1);

                                                            driverInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                                Toast.makeText(DriverHome.this, "Password was changed", Toast.LENGTH_SHORT).show();
                                                                            else
                                                                                Toast.makeText(DriverHome.this, "Password was changed but not update to your database", Toast.LENGTH_SHORT).show();
                                                                            waitingDialog.dismiss();
                                                                        }
                                                                    });

                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(DriverHome.this, "Password doesn't change", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });

                                    }
                                    else
                                    {
                                        waitingDialog.dismiss();
                                        Toast.makeText(DriverHome.this,"Wrong old password",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }

                else {
                    waitingDialog.dismiss();
                    Toast.makeText(DriverHome.this,"Password doesn't match",Toast.LENGTH_SHORT).show();
                }


            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void signOut() {

        //reset  remember value
        Paper.init(this);
        Paper.book().destroy();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverHome.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
