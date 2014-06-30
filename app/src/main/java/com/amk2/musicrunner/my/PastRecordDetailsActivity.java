package com.amk2.musicrunner.my;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.LocationUtils;
import com.amk2.musicrunner.running.MapFragmentRun;
import com.amk2.musicrunner.services.SyncService;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.utilities.ColorGenerator;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by daz on 2014/6/28.
 */
public class PastRecordDetailsActivity extends Activity {

    public static String PAST_RECORD_ID = "com.amk2.id";

    private String id;

    private ContentResolver mContentResolver;

    private ArrayList<LatLng> mLocationList = null;
    private ArrayList<Integer> mColorList = null;

    private GoogleMap mMap;

    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView speedTextView;
    private TextView finishTimeTextView;
    private TextView pastRecordDateTextView;
    private ImageView photoImageView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.past_record_details_activity);
        mContentResolver = getContentResolver();

        initializeViews();
        setViews();
    }

    private void initializeViews() {
        distanceTextView = (TextView) findViewById(R.id.past_record_details_distance);
        caloriesTextView = (TextView) findViewById(R.id.past_record_details_calories);
        speedTextView    = (TextView) findViewById(R.id.past_record_details_speed);
        photoImageView   = (ImageView) findViewById(R.id.past_record_details_photo);
        finishTimeTextView = (TextView) findViewById(R.id.finish_time);
        pastRecordDateTextView = (TextView) findViewById(R.id.past_record_date);

        mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.past_record_map)).getMap();
    }

    private void setViews () {
        Intent intent = getIntent();

        int durationInSec;
        long timeInMillis;
        String timeInMillisString;
        String distance, calories, speed, photoPath, route;

        String[] projection = {
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ROUTE
        };
        String selection = MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ID + " LIKE ?";
        id  = intent.getStringExtra(PAST_RECORD_ID);
        String[] selectionArgs = { id };

        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackRunningEventDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        durationInSec      = cursor.getInt(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION));
        timeInMillisString = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND));
        distance           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE));
        calories           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES));
        speed              = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED));
        photoPath          = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH));
        route              = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_ROUTE));
        timeInMillis       = Long.parseLong(timeInMillisString);

        HashMap<String, Integer> readableTime = TimeConverter.getReadableTimeFormatFromSeconds(durationInSec);
        String durationString = TimeConverter.getDurationString(readableTime);
        String pastRecordDate = TimeConverter.getDateString(timeInMillis);

        distanceTextView.setText(distance);
        caloriesTextView.setText(calories);
        speedTextView.setText(speed);

        mLocationList = LocationUtils.parseRouteToLocation(route);
        mColorList = LocationUtils.parseRouteColor(route);
        mDrawRoute();

        finishTimeTextView.setText(durationString);
        pastRecordDateTextView.setText(pastRecordDate);
        if (photoPath != null) {
            Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, photoImageView.getLayoutParams().width, photoImageView.getLayoutParams().height);
            photoImageView.setImageBitmap(resizedPhoto);
        }
    }

    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void mDrawRoute() {
        if(mLocationList.size() > 0) {
            LatLng lastPosition = null;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocationList.get(0), LocationUtils.CAMERA_PAD));

            for (int i = 0; i < mLocationList.size(); i++) {
                if(lastPosition != null) {
                    mMap.addPolyline(
                            new PolylineOptions()
                                    .geodesic(true)
                                    .color(ColorGenerator
                                            .generateColor(mColorList.get(i)))
                                    .add(lastPosition)
                                    .add(mLocationList.get(i))
                    );
                }
                lastPosition = mLocationList.get(i);
            }
        }
    }

}
