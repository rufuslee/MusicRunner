package com.amk2.musicrunner.finish;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackRunningEventDataDB;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.TimeConverter;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by daz on 2014/6/15.
 */
public class FinishRunningActivity extends Activity implements View.OnClickListener{
    public static String FINISH_RUNNING_DURATION = "com.amk2.duration";
    public static String FINISH_RUNNING_DISTANCE = "com.amk2.distance";
    public static String FINISH_RUNNING_CALORIES = "com.amk2.calories";
    public static String FINISH_RUNNING_SPEED    = "com.amk2.speed";
    public static String FINISH_RUNNING_PHOTO    = "com.amk2.photo";

    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView speedTextView;
    private TextView finishTimeTextView;
    private ImageView photoImageView;

    private int totalSec = 0;
    private String distance  = null;
    private String calories  = null;
    private String speed     = null;
    private String photoPath = null;

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

        distanceTextView = (TextView) findViewById(R.id.finish_running_distance);
        caloriesTextView = (TextView) findViewById(R.id.finish_running_calories);
        speedTextView    = (TextView) findViewById(R.id.finish_running_speed);
        photoImageView   = (ImageView) findViewById(R.id.finish_running_photo);
        finishTimeTextView = (TextView) findViewById(R.id.finish_time);

        totalSec  = intent.getIntExtra(FINISH_RUNNING_DURATION, 0);
        distance  = intent.getStringExtra(FINISH_RUNNING_DISTANCE);
        calories  = intent.getStringExtra(FINISH_RUNNING_CALORIES);
        speed     = intent.getStringExtra(FINISH_RUNNING_SPEED);
        photoPath = intent.getStringExtra(FINISH_RUNNING_PHOTO);

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
                //values.put(MusicTrackRunningEventDataDB.COLUMN_NAME_ROUTE, "someroute, type should be string");
                Uri uri = mContentResolver.insert(MusicTrackRunningEventDataDB.CONTENT_URI, values);

                Log.d("Save running event, uri=", uri.toString());

                finish();
                break;
            case R.id.discard_running_event:
                Log.d("daz", "discard running event");
                finish();
                break;
        }
    }
}
