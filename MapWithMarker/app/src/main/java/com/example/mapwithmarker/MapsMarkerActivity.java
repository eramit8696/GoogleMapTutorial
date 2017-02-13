package com.example.mapwithmarker;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
//import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;

import android.util.Log;
import android.widget.TextView;

import com.example.mapwithmarker.data.LocationDbHelper;
import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.example.mapwithmarker.data.LocationContract.LocationEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.mapwithmarker.data.LocationDbHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapsMarkerActivity extends AppCompatActivity
        implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String LOG_TAG = "LocationApp";
    private LocationDbHelper mDbHelper;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String USGS_REQUEST_URL_FIRST ="https://roads.googleapis.com/v1/snapToRoads?path=";
    private String USGS_REQUEST_URL_COMPLETE="";
    private String USGS_REQUEST_URL_LAST ="&interpolate=false&key=AIzaSyCmrznvzvIYlDSMCaJB4MxVrqO8EbJalkc";
    private SupportMapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDbHelper = new LocationDbHelper(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(-33.852, 151.211);
        //googleMap.addMarker(new MarkerOptions().position(sydney)
          //      .title("Marker in Sydney"));
        //googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000);
        /*if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }*/
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
    }

    @Override
    public void onLocationChanged(Location location) {
        //displayDatabaseInfo();
        Log.e("LOG_TAG", location.toString());
        EartquakeAsyncTask task = new EartquakeAsyncTask();
        USGS_REQUEST_URL_COMPLETE=USGS_REQUEST_URL_FIRST+location.getLatitude()+","+location.getLongitude()+USGS_REQUEST_URL_LAST;
        task.execute(USGS_REQUEST_URL_COMPLETE);

       // txtOutput.setText(Double.toString(location.getLatitude()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("LOG_TAG", " Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("LOG_TAG", " Not Connectionted");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.e("Start", " Method");
    }

    @Override
    protected void onStop() {
        Log.e("Stop", " Method");
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    private void updateUi(Event earthquake) {
      /* TextView titleTextView = (TextView) findViewById(R.id.txtOutput);
        titleTextView.setText(earthquake.latitude);*/
        LatLng sydney = new LatLng(Double.parseDouble(earthquake.latitude), Double.parseDouble(earthquake.longitude));
        mapFragment.getMap().addMarker(new MarkerOptions().position(sydney));
        mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mapFragment.getMap().getMaxZoomLevel();
        //map = (GoogleMap) findViewById(R.id.map);


    }

    private class EartquakeAsyncTask extends AsyncTask<String, Void, Event> {


        @Override
        protected Event doInBackground(String... urls) {
            Event result = Utils.fetchEarthquakeData(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(Event result) {
            insertPet(Double.parseDouble(result.latitude),Double.parseDouble(result.longitude));
            displayDatabaseInfo();
            updateUi(result);
        }
    }
    private static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LATITUDE,
                LocationEntry.COLUMN_LONGITUDE,
                LocationEntry.COLUMN_TIMESTAMP
        };

        // Perform a query on the pets table
        Cursor cursor = db.query(
                LocationEntry.TABLE_NAME,   // The table to query
                projection,            // The columns to return
                null,                  // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // The sort order
        try {
            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(LocationEntry._ID);
            int latColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LATITUDE);
            int longColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_LONGITUDE);
            int timeColumnIndex = cursor.getColumnIndex(LocationEntry.COLUMN_TIMESTAMP);

            while (cursor.moveToNext()) {
                int currentID = cursor.getInt(idColumnIndex);
                Double lat = cursor.getDouble(latColumnIndex);
                Double longitude = cursor.getDouble(longColumnIndex);
                String mtime = cursor.getString(timeColumnIndex);
                Log.e(" Amit ","Id "+currentID+" Lat Longitude "+lat+","+longitude+" Time "+mtime);
            }


        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }
    private  void insertPet(double lat,double longi) {
        // Gets the database in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        // values.put(LocationEntry._ID, "1");
        values.put(LocationEntry.COLUMN_LATITUDE, lat);
        values.put(LocationEntry.COLUMN_LONGITUDE,longi);
        values.put(LocationEntry.COLUMN_TIMESTAMP, getDateTime());

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the pets table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        long newRowId = db.insert(LocationEntry.TABLE_NAME, null, values);
    }
}
