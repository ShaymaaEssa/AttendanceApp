package nanodegree.mal.udacity.android.attendenceapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    Button btn_startService;
    Button btn_stopService;
    Button btn_startGeofence;

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient; //object of Google Api Client
    private Location lastLocation;           //current location
    private LocationRequest locationRequest; //to ask for location update

    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    //private final int UPDATE_INTERVAL =  10 * 60 * 1000; // 10 minutes
    //private final int FASTEST_INTERVAL = 5 * 60 * 1000;  //  5 minutes
    private final int UPDATE_INTERVAL = 1000; //listen for location update every ...
    private final int FASTEST_INTERVAL = 900;
    private final int REQ_PERMISSION = 1;       //for request permission (location update)  if it is not granted

    //for creation of GeoFence
    private static final long GEO_DURATION = 60 * 60 * 1000; //geofence will be removed automatically after one hour.
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 1.0f; // in meters

    //Geofence request
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_startService = (Button) findViewById(R.id.btn_main_startservice);
        btn_stopService = (Button) findViewById(R.id.btn_main_stopservice);
        btn_startGeofence = (Button) findViewById(R.id.btn_main_startgeofence);

        btn_startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //create google api client
                createGoogleApi();
            }
        });

        btn_startGeofence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startGeofence();
            }
        });
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        //Google API Client
        //Google API Client provides a common entry point to all the Google Play services
        // and manages the network connection between the user's device and each Google service.
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }

        //connect to googleapiclient
        if (!googleApiClient.isConnected())
            googleApiClient.connect();

    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }

    // get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");

        if (checkPermission()) { //Check if the user gave the application the appropriate permissions
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else { //lastlocation == null
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }

        } else {
            askPermission(); //ask user for the permission to get his location
        }

    }


    //start Location Update
    public void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");

        //the difference between setInterval and setFastestInterval
        //setInterval(long) means - set the interval in which you want to get locations.
        //setFastestInterval(long) means - if a location is available sooner you can get it.
        //For example, you start your application and register it via setInterval(60*1000),
        // that means that you'll get updates every 60 seconds.
        //Now you call setFastestInterval(10*1000).
        // If you are the only app which use the location services you will continue to receive updates every 60 seconds.
        // If another app is using the location services with a higher rate of updates, you will get more location updates.

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this); //this : the listener for the location update ---> LocationListener

    }

    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);
    }

    //for debugging // should be deleted
    private void writeActualLocation(Location location) {
        Toast.makeText(this, "Updated Location: " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    //for debugging // should be deleted
    private void writeLastLocation() {
        Toast.makeText(this, "Current Location: " + lastLocation.getLatitude() + "," + lastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
    }

    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");

        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED);
    }

    //ask for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult()");
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    getLastKnownLocation();
                } else {
                    //permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    //App can't work without permission
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        Toast.makeText(this, "App can't work without permission", Toast.LENGTH_SHORT).show();
    }

    //create geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION) //This geofence will be removed automatically after this period of time.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    //create geofence request
    private GeofencingRequest createGeofenceRequest (Geofence geofence){
        Log.d(TAG, "createGeofenceRequest");

        //INITIAL_TRIGGER_ENTER
        //A flag indicating that geofencing service should trigger GEOFENCE_TRANSITION_ENTER notification
        // at the moment when the geofence is added and if the device is already inside that geofence.
        return  new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_EXIT) //Sets the geofence notification behavior at the moment when the geofences are added.
                .addGeofence(geofence)
                .build();
    }

    //it will used in addGeofence ()
    private PendingIntent createGeofencePendingIntent (){
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

        Intent intent = new Intent(this,GeofenceTransitionService.class);

        //PendingIntent:
        //It's a token that your app process will give to the location process,
        // and the location process will use it to wake up your app when an event of interest happens.
        // So this basically means that your app in the background doesn't have to be always running.
        // When something of interest happens, we will wake you up. This saves a lot of battery.

        //PendingIntent.getService
        //Retrieve a PendingIntent that will start a service
        //intent	Intent: An Intent describing the service to be started.

        //FLAG_UPDATE_CURRENT
        //Flag indicating that if the described PendingIntent already exists,
        // then keep it but replace its extra data with what is in this new Intent.
        PendingIntent pendingIntent = PendingIntent.getService(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;

    }

    public void addGeofenceMethod(GeofencingRequest request){
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }


    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess())
            Toast.makeText(this,"geofence added successfully",Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "geofence added failed", Toast.LENGTH_SHORT).show();
    }

    public LatLng getCompanyGeofenceLocation (){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        double lat = Double.parseDouble(preferences.getString("GEOFENCE_LOCATION_LAT", ""));
        double lng = Double.parseDouble(preferences.getString("GEOFENCE_LOCATION_LNG", ""));

        return new LatLng(lat,lng);  //company Geofence Location


    }

    public void startGeofence(){
        Log.i(TAG, "startGeofence()");

        Geofence geofence = createGeofence(getCompanyGeofenceLocation(), GEOFENCE_RADIUS);
        GeofencingRequest geofenceRequest  = createGeofenceRequest(geofence);
        addGeofenceMethod(geofenceRequest);
    }

    private static final int PRIORITY_HIGH = 5;
    public static Intent makeNotificationIntent(Context context, String message){
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return notificationIntent;
//        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.ic_action_location)
//                .setContentTitle(context.getString(R.string.app_name))
//                .setContentIntent(intent)
//                .setPriority(PRIORITY_HIGH) //private static final PRIORITY_HIGH = 5;
//                .setContentText(message)
//                .setAutoCancel(true)
//                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
//        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        mNotificationManager.notify(0, mBuilder.build());
    }

}