package com.amk2.musicrunner.finish;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.LocationUtils;
import com.amk2.musicrunner.running.MapFragmentRun;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackRunningEventDataDB;
import com.amk2.musicrunner.utilities.ColorGenerator;
import com.amk2.musicrunner.utilities.Comparators;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.ShowImageActivity;
import com.amk2.musicrunner.utilities.SongPerformance;
import com.amk2.musicrunner.utilities.TimeConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.Inflater;

import static android.widget.Toast.makeText;
import static com.amk2.musicrunner.utilities.Comparators.*;

/**
 * Created by daz on 2014/6/15.
 */
public class FinishRunningActivity extends Activity implements View.OnClickListener{
    public static String FINISH_RUNNING_DURATION = "com.amk2.duration";
    public static String FINISH_RUNNING_DISTANCE = "com.amk2.distance";
    public static String FINISH_RUNNING_CALORIES = "com.amk2.calories";
    public static String FINISH_RUNNING_SPEED    = "com.amk2.speed";
    public static String FINISH_RUNNING_PHOTO    = "com.amk2.photo";
    public static String FINISH_RUNNING_SONGS    = "com.amk2.songs";

    private LayoutInflater inflater;

    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView speedTextView;
    private TextView finishTimeTextView;
    private ImageView photoImageView;
    private LinearLayout musicListLinearLayout;

    private GoogleMap mMap;

    private int totalSec = 0;
    private String distance  = null;
    private String calories  = null;
    private String speed     = null;
    private String photoPath = null;
    private String route     = null;
    private String songNames = null;

    private Button saveButton;
    private Button discardButton;

    private ContentResolver mContentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_running);

        mContentResolver = getContentResolver();

        Intent intent = getIntent();

        saveButton    = (Button) findViewById(R.id.save_running_event);
        discardButton = (Button) findViewById(R.id.discard_running_event);

        saveButton.setOnClickListener(this);
        discardButton.setOnClickListener(this);

        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        distanceTextView = (TextView) findViewById(R.id.finish_running_distance);
        caloriesTextView = (TextView) findViewById(R.id.finish_running_calories);
        speedTextView    = (TextView) findViewById(R.id.finish_running_speed);
        photoImageView   = (ImageView) findViewById(R.id.finish_running_photo);
        finishTimeTextView = (TextView) findViewById(R.id.finish_time);
        musicListLinearLayout = (LinearLayout) findViewById(R.id.music_result_holder);

        totalSec  = intent.getIntExtra(FINISH_RUNNING_DURATION, 0);
        distance  = intent.getStringExtra(FINISH_RUNNING_DISTANCE);
        calories  = intent.getStringExtra(FINISH_RUNNING_CALORIES);
        speed     = intent.getStringExtra(FINISH_RUNNING_SPEED);
        photoPath = intent.getStringExtra(FINISH_RUNNING_PHOTO);
        songNames = intent.getStringExtra(FINISH_RUNNING_SONGS);

        photoImageView.setOnClickListener(this);

        if (totalSec > 0) {
            HashMap<String, Integer> time =  TimeConverter.getReadableTimeFormatFromSeconds(totalSec);
            String duration = TimeConverter.getDurationString(time);
            finishTimeTextView.setText(duration);
        }
        if (distance != null) {
            distanceTextView.setText(distance);
        }
        if (calories != null) {
            caloriesTextView.setText(calories);
        }
        if (speed != null) {
            speedTextView.setText(speed);
        }
        if (photoPath != null) {
            Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, photoImageView.getLayoutParams().width, photoImageView.getLayoutParams().height);
            photoImageView.setImageBitmap(resizedPhoto);
        }
        if (songNames != null) {
            addSongNames();
        }

        // Get a handle to the Map Fragment
        mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.finish_running_map)).getMap();
        // Draw the map base on last run
        mDrawRoute();

    }

    @Override
    protected void onStart () {
        super.onStart();
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.save_running_event:
                Calendar calendar = Calendar.getInstance();
                long timeInMillis = calendar.getTimeInMillis();
                ContentValues values = new ContentValues();
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_DURATION, totalSec);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_DATE_IN_MILLISECOND, Long.toString(timeInMillis));
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES, calories);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE, distance);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED, speed);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_PHOTO_PATH, photoPath);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_ROUTE, route);
                values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_SONGS, songNames);
                Uri uri = mContentResolver.insert(MusicTrackRunningEventDataDB.CONTENT_URI, values);

                Log.d("Save running event, uri=", uri.toString());

                finish();
                break;
            case R.id.discard_running_event:
                Log.d("daz", "discard running event");
                finish();
                break;
            case R.id.finish_running_photo:
                if (photoPath != null) {
                    Intent intent = new Intent(this, ShowImageActivity.class);
                    intent.putExtra(ShowImageActivity.PHOTO_PATH, photoPath);
                    startActivity(intent);
                }
                break;
        }
        MapFragmentRun.resetAllParam();
    }

    private void mDrawRoute() {
        ArrayList<LatLng> polylines = MapFragmentRun.getmTrackList();
        ArrayList<Integer> mColorList = MapFragmentRun.getmColorList();
        if(polylines.size() > 0) {
            LatLng lastPosition = null;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(polylines.get(0), LocationUtils.CAMERA_PAD));

            for (int i = 0; i < polylines.size(); i++) {
                if(lastPosition != null) {
                    mMap.addPolyline(
                            new PolylineOptions()
                                    .geodesic(true)
                                    .color(ColorGenerator
                                    .generateColor(mColorList.get(i)))
                                    .add(lastPosition)
                                    .add(polylines.get(i))
                    );
                    route = (route == null) ?
                            (LocationUtils.getLatLng(lastPosition, mColorList.get(i))) :
                            (route + LocationUtils.getLatLng(lastPosition, mColorList.get(i)));
                }
                lastPosition = polylines.get(i);
            }
        }
    }

    private void addSongNames() {
        ArrayList<SongPerformance> songPerformanceArrayList = new ArrayList<SongPerformance>();
        for (String song : songNames.split(Constant.SONG_SEPARATOR)) {
            if (song.length() > 0) {
                String[] songXperf = song.split(Constant.PERF_SEPARATOR);
                Double perf = Double.parseDouble(songXperf[1]);
                songPerformanceArrayList.add(new SongPerformance(songXperf[0], perf));
            }
        }
        songNames = "";
        if (songPerformanceArrayList.size() > 0) {
            Collections.sort(songPerformanceArrayList, new Comparators.SongPerformanceComparators());
            int max = 3;
            for (int i = 0; i < songPerformanceArrayList.size() && i < max; i++) {
                String songName = songPerformanceArrayList.get(i).getSong();
                String performanceString = songPerformanceArrayList.get(i).getPerformance().toString();
                Log.d("sorted song", performanceString + "," + songName);

                View finishMusic = inflater.inflate(R.layout.finish_music_template, null);
                TextView songNameTextView = (TextView) finishMusic.findViewById(R.id.song_name);
                songNameTextView.setText((i+1) + ". " + songPerformanceArrayList.get(i).getSong() + ", " + songPerformanceArrayList.get(i).getPerformance().toString() + " kcal/min");
                musicListLinearLayout.addView(finishMusic);

                songNames += (songName + Constant.PERF_SEPARATOR + performanceString + Constant.SONG_SEPARATOR );
            }
        }
    }
}
