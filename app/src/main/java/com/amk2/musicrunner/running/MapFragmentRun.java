package com.amk2.musicrunner.running;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MapFragmentRun extends Fragment implements
        com.google.android.gms.location.LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap = null; // Might be null if Google Play services APK is not available.
    private Marker mMarker = null;

    private LatLng mlastLoc = null;
    private ArrayList<LatLng> mTrackList;

    private static double mSpeed;
    private static double mTotalDistance;
    private static String mRoute;

    private double mTolerance = 0.5;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    boolean mUpdatesRequested = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_fragment_run, container, false);
        setUpMapIfNeeded();

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;

        // Open Shared Preferences
        mPrefs = this.getActivity().getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(v.getContext(), this, this);

        return v;
    }


    @Override
    public void onStop() {

        stopUpdates(this.getView());
        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Connect the client.
        mLocationClient.connect();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onPause() {

        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

            // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }

        setUpMapIfNeeded();

    }


    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this.getView().getContext(), "Connected", Toast.LENGTH_SHORT).show();

        getLocation(this.getView());
        startUpdates(this.getView());
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this.getView().getContext(), "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        LatLng curLoc = new LatLng(lat, lng);

        if (mMarker != null)
            mMarker.remove();
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 15));

        mMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.fox))
                .anchor(0.0f, 1.0f)
                .position(curLoc).title("Yo"));


        drawLine(curLoc);
    }

    private void drawLine(LatLng curr) {
        double distance = 0;
        if (mTrackList == null) {
            mTrackList = new ArrayList<LatLng>();
        }
        if (mlastLoc == null) {
            mlastLoc = curr;
            mTrackList.add(new LatLng(curr.latitude, curr.longitude));
        } else {
            distance = CalculationByDistance(mlastLoc.latitude, mlastLoc.longitude, curr.latitude, curr.longitude);

            // draw the line, and save it.
            if (distance > LocationUtils.MIN_DISTANCE) {
                mTolerance = distance;
                mTotalDistance += distance;
                mSpeed = distance / LocationUtils.UPDATE_INTERVAL_IN_SECONDS;
                mTrackList.add(curr);
                PolylineOptions polylineOpt = new PolylineOptions();
                polylineOpt.add(mlastLoc).add(curr);
                polylineOpt.color(Color.RED);
                Polyline line = mMap.addPolyline(polylineOpt);
                line.setWidth(LocationUtils.LINE_WIDTH);

                String locString = LocationUtils.getLatLng(this.getView().getContext(), curr);
                if (locString != LocationUtils.EMPTY_STRING) {
                    mRoute = (mRoute == null) ? (locString + "@") : (mRoute + (locString + "@"));
                }
                mlastLoc = curr;
            } else {
                mSpeed = 0;
            }

            Toast.makeText(this.getView().getContext(), "mDistance = " + mTotalDistance + ", mSpeed = " + mSpeed,
                    Toast.LENGTH_SHORT).show();

        }
    }

    private double CalculationByDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        double earthRadius = LocationUtils.EARTH_RADIOUS;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = LocationUtils.METER_CONVERSTION;

        return new Float(distance * meterConversion).floatValue();
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play services was not available for some reason
        }
        return false;
    }

    public void getLocation(View v) {

        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();
        }
    }

    public void startUpdates(View v) {
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    public void stopUpdates(View v) {
        mUpdatesRequested = false;

        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this.getActivity(),
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            Toast.makeText(this.getView().getContext(), "onConnectionFailed Error code : " + connectionResult.getErrorCode(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

                        // Display the result
                        /*
                        mConnectionState.setText(R.string.connected);
                        mConnectionStatus.setText(R.string.resolved);
                        */
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));

                        // Display the result
                        /*
                        mConnectionState.setText(R.string.disconnected);
                        mConnectionStatus.setText(R.string.no_resolution);
                        */
                        break;
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(LocationUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }


    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates((com.google.android.gms.location.LocationListener) this);
    }

    private void startPeriodicUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.running_map)).getMap();
        }
    }

    public static double getmTotalDistance() {
        return mTotalDistance;
    }

    public static double getmSpeed() {
        return mSpeed;
    }

    public static String getmRoute() { return mRoute; }

    public static void resetAllParam() {
        MapFragmentRun.mTotalDistance = 0;
        MapFragmentRun.mSpeed = 0;
        MapFragmentRun.mRoute = "";
    }
}
