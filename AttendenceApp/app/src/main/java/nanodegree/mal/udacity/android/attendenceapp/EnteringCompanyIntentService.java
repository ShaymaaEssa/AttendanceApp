package nanodegree.mal.udacity.android.attendenceapp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by MOSTAFA on 04/10/2016.
 */
public class EnteringCompanyIntentService extends IntentService {
    private static final String TAG = EnteringCompanyIntentService.class.getSimpleName();

    public EnteringCompanyIntentService (){
        super(TAG);
    }



    @Override
    protected void onHandleIntent(Intent intent) {

        // Retrieve the Geofencing intent
        //get geofencingevent from the received intent.
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent != null){
            if (geofencingEvent.hasError()){
                String errorMsg = onError(geofencingEvent.getErrorCode());
                Log.e(TAG, errorMsg);
                Toast.makeText(getApplication(),errorMsg,Toast.LENGTH_SHORT).show();
                return;
            }
            //We then check if the kind of geofencing transition that took place is of interest to us:
            else {
                //returns the GEOFENCE_TRANSITION_ flags value defined in Geofence
                //Get the transition type
                int geoFenceTransition = geofencingEvent.getGeofenceTransition();

                if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){

                }

            }

            }
        }

    // Handle errors
    private static String onError(int errorCode) {
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


