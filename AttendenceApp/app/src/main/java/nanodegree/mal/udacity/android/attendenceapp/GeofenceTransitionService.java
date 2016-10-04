package nanodegree.mal.udacity.android.attendenceapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MOSTAFA on 02/10/2016.
 */

//GeofenceTransitionService class responsible for handling the GeofencingEvent
public class GeofenceTransitionService extends IntentService {

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();
    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    public GeofenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve the Geofencing intent
        //get geofencingevent from the received intent.
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        //handling error
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e(TAG, errorMsg);
            return;
        }

        //We then check if the kind of geofencing transition that took place is of interest to us:

        //returns the GEOFENCE_TRANSITION_ flags value defined in Geofence
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        Toast.makeText(getApplicationContext(),"Intent test "+ geoFenceTransition , Toast.LENGTH_SHORT).show();

        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            //we retrieve a list of the triggered geofences and create a notification with the appropriate actions.

            //// Get the geofence that were triggered
            List<Geofence> triggeringGeoFences = geofencingEvent.getTriggeringGeofences();

            //create a detail msg with geofence received
            String geofenceTransitionDetail = getGeofenceTransitionDetails(geoFenceTransition,triggeringGeoFences);

            //send notification detail with string
            sendNotification(geofenceTransitionDetail);

            Toast.makeText(getApplicationContext(),"GeofenceEntering",Toast.LENGTH_LONG).show();

        }
    }


    private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeoFences) {
        ArrayList<String> triggeringGeoFencesList = new ArrayList<>();
        for (Geofence geofence : triggeringGeoFences){
            triggeringGeoFencesList.add(geofence.getRequestId());
        }

        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            status = "Entering";

        return status + TextUtils.join(", ",triggeringGeoFencesList);

    }

    //send a notification
    private void sendNotification(String msg) {
        Log.i(TAG, "sendNotification: " + msg );

        // Intent to start the main Activity
        Intent notificationIntent = MainActivity.makeNotificationIntent(
                getApplicationContext(), msg
        );

//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(notificationIntent);
//        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // 2. Create a PendingIntent
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));
    }

    // Create a notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_action_location)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }



}
