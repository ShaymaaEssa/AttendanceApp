package nanodegree.mal.udacity.android.attendenceapp;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nanodegree.mal.udacity.android.attendenceapp.DataBase.AttendanceDBHelper;


public class RegisterActivity extends ActionBarActivity {

    EditText etxt_name;
    EditText etxt_companyName;
    EditText etxt_lat;
    EditText etxt_long;
    EditText etxt_workFrom;
    EditText etxt_workTo;
    CheckBox chbox_inCompany;
    Button btn_save;
    Button btn_daysoff;


    List<String> weekdaysList = new ArrayList<String>();
    String selectedDaysOff;

    LocationManager locMan;
    String lat;
    String lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //to initialize the controls
        init();

        //if inCompany checkbox is checked then disable long and lat edit text field and vise verse.
        chbox_inCompany.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    etxt_lat.setEnabled(false);
                    etxt_long.setEnabled(false);
                }
                else {
                    etxt_lat.setEnabled(true);
                    etxt_long.setEnabled(true);
                }
            }
        });

        //to set alertdialog for selecting the days off
        btn_daysoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CharSequence[] dialogList=  weekdaysList.toArray(new CharSequence[weekdaysList.size()]);
                final AlertDialog.Builder builderDialog = new AlertDialog.Builder(RegisterActivity.this);
                builderDialog.setTitle("Select Your WeekEnd");
                int count = dialogList.length;
                boolean[] is_checked = new boolean[count];

                // Creating multiple selection by using setMutliChoiceItem method
                builderDialog.setMultiChoiceItems(dialogList, is_checked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton, boolean isChecked) {
                            }
                        });

                builderDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ListView list = ((AlertDialog) dialog).getListView();
                        // make selected item in the comma seprated string
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < list.getCount(); i++) {
                            boolean checked = list.isItemChecked(i);

                            if (checked) {
                                if (stringBuilder.length() > 0) stringBuilder.append(",");
                                stringBuilder.append(list.getItemAtPosition(i));


                            }
                        }

                        /*Check string builder is empty or not. If string builder is not empty.
                          It will display on the screen.
                         */
                        if (stringBuilder.toString().trim().equals("")) {
                            stringBuilder.setLength(0);

                        } else {

                            selectedDaysOff = stringBuilder.toString();
                        }

                        btn_daysoff.setText("Your days off: " + selectedDaysOff);
                    }
                });
                builderDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectedDaysOff = "";
                            }
                        });

                AlertDialog alert = builderDialog.create();
                alert.show();
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isLocationSet = false;
                if (validateParameters()){

                    //to get the current location of the user
                    if (chbox_inCompany.isChecked()){
                        isLocationSet = getTheCurrentLocation ();
                    }
                    else {
                        lat = etxt_lat.getText().toString();
                        lng = etxt_long.getText().toString();
                        isLocationSet = true;
                    }

                    if (isLocationSet){
                        //insert UserInfo into DataBase
                        addUserInfoToDB();

                        //to set the first time the application run with false
                        MyPreferences.editFirst();

                        //to save company location in shared preference:
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("GEOFENCE_LOCATION_LAT", lat);
                        editor.putString("GEOFENCE_LOCATION_LNG", lng);
                        editor.apply();

                        //start mainactivity
                        Intent mainActivityIntent = new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(mainActivityIntent);

                    }

                }

            }
        });
    }

    // insert user info into the database
    private void addUserInfoToDB() {
        SQLiteDatabase db;
        long insertId;
        try{

            ContentValues userValues = new ContentValues();
            userValues.put("UserName",etxt_name.getText().toString());
            userValues.put("CompanyName",etxt_companyName.getText().toString());
            userValues.put("Latitude",etxt_lat.getText().toString());
            userValues.put("Longitude",etxt_long.getText().toString());
            userValues.put("WorkTimeFrom",etxt_workTo.getText().toString());
            userValues.put("WorkTimeTo", etxt_workTo.getText().toString());
            userValues.put("DaysOff",selectedDaysOff);


            SQLiteOpenHelper attendanceDBHelper = new AttendanceDBHelper(RegisterActivity.this);
            db = attendanceDBHelper.getWritableDatabase();
            insertId = db.insert("UserInfo", null, userValues);
            if (insertId == -1)
                Toast.makeText(this, "Error in Adding UserInfo to the DataBase", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "UserInfo Added", Toast.LENGTH_SHORT).show();
        }catch (SQLiteException exception){
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    //method to get the current location (lat-lng) of the user
    private boolean getTheCurrentLocation() {
        locMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        //String bestProvider = locMan.getBestProvider(criteria, true);
        //Location lastLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Location lastLoc = getLastKnownLocation();
        if(lastLoc != null){
            lat= String.valueOf(lastLoc.getLatitude());
            lng = String.valueOf(lastLoc.getLongitude());
            return true;

           // Toast.makeText(RegisterActivity.this, "lat = " + lat + " lng = " + lng, Toast.LENGTH_LONG).show();
        }

        else {
            Toast.makeText(RegisterActivity.this, "Can't get the current location", Toast.LENGTH_SHORT).show();
            return false;

        }
    }

    private boolean validateParameters() {
        String validateName = etxt_name.getText().toString();
        String validateLat = etxt_lat.getText().toString();
        String validateLong = etxt_long.getText().toString();
        boolean validateInCompany = chbox_inCompany.isChecked();
        String validateWorkFrom = etxt_workFrom.getText().toString();
        String validateWorkTo = etxt_workTo.getText().toString();


        if(TextUtils.isEmpty(validateName) || validateName.length() < 4) {
            etxt_name.setError("Please Enter Your Full Name");
            return false;
        }

        if (!validateInCompany ){
            if (TextUtils.isEmpty(validateLat) || (Double.parseDouble(validateLat) < -90 || Double.parseDouble(validateLat) > 90)){
                etxt_lat.setError("Please Enter A Valid Location Latitude");
                return false;
            }

            else if (TextUtils.isEmpty(validateLong) || (Double.parseDouble(validateLong) < -180 || Double.parseDouble(validateLong) > 180)){
                etxt_lat.setError("Please Enter A Valid Location Longitude");
                return false;
            }
        }

        if(TextUtils.isEmpty(validateWorkFrom)) {
            etxt_workFrom.setError("Please Specify Work From Time");
            return false;
        }

        if(TextUtils.isEmpty(validateWorkTo)) {
            etxt_workTo.setError("Please Specify Work To Time");
            return false;
        }
        return true;
    }

    private void init() {
        etxt_name = (EditText)findViewById(R.id.etxt_register_name);
        etxt_companyName = (EditText)findViewById(R.id.etxt_register_companyname);
        etxt_lat = (EditText)findViewById(R.id.etxt_register_latitude);
        etxt_long = (EditText)findViewById(R.id.etxt_register_longitude);
        etxt_workFrom = (EditText)findViewById(R.id.etxt_register_timefrom);
        etxt_workTo = (EditText)findViewById(R.id.etxt_register_timeto);
        chbox_inCompany = (CheckBox)findViewById(R.id.chbox_register_incompany);
        btn_save = (Button)findViewById(R.id.btn_register_save);
        btn_daysoff = (Button)findViewById(R.id.btn_register_holiday);

        weekdaysList.add("Friday");
        weekdaysList.add("Saturday");
        weekdaysList.add("Sunday");
        weekdaysList.add("Monday");
        weekdaysList.add("Tuesday");
        weekdaysList.add("Wednesday");
        weekdaysList.add("Thursday");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    private Location getLastKnownLocation() {
        List<String> providers = locMan.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locMan.getLastKnownLocation(provider);
           // Log.d("last known location, provider: %s, location: %s", provider, l);

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
               // Log.d("found best last known location: %s", l);
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }
}
