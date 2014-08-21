package com.amk2.musicrunner.start;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.location.LocationMetaData;
import com.amk2.musicrunner.services.SyncService;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData.MusicTrackCommonDataDB;
import com.amk2.musicrunner.observers.TableObserver;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicService;
import com.amk2.musicrunner.running.RunningActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by daz on 2014/4/22.
 */
public class StartFragment extends Fragment implements View.OnClickListener {

    public interface StartTabFragmentListener {
    	void onSwitchBetweenStartAndWeatherFragment();
    }

    private Activity mActivity;
    private Intent mStartMusicServiceIntent;

    private ImageButton mGoRunningButton;
    private TextView chanceOfRain;
    private TextView uvIndex;
    private TextView humidity;
    private TextView startTemperature;
    private TextView suggestionDialog;
    private TextView city;
    private LinearLayout startTemperatureContainer;

    //private StartTabFragmentListener mStartTabFragmentListener;

    private ContentResolver mContentResolver;

    //public void setStartTabFragmentListener(StartTabFragmentListener listener) {
    	//mStartTabFragmentListener = listener;
    //}

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_fragment, container, false);
    }
    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        mActivity = getActivity();
        mStartMusicServiceIntent = new Intent(getActivity(),MusicService.class);
        View thisView = getView();
        mGoRunningButton = (ImageButton) thisView.findViewById(R.id.go_running_button);
        chanceOfRain     = (TextView) thisView.findViewById(R.id.chance_of_rain_container);
        uvIndex          = (TextView) thisView.findViewById(R.id.uv_index_container);
        humidity         = (TextView) thisView.findViewById(R.id.humidity_container);
        startTemperature = (TextView) thisView.findViewById(R.id.start_temperature);
        suggestionDialog = (TextView) thisView.findViewById(R.id.suggestion_dialog);
        city             = (TextView) thisView.findViewById(R.id.city);
        startTemperatureContainer = (LinearLayout) thisView.findViewById(R.id.start_temperature_container);
        startTemperatureContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Switch to WeatherFragment
            	//mStartTabFragmentListener.onSwitchBetweenStartAndWeatherFragment();
            }
        });
        mGoRunningButton.setOnClickListener(this);

        //checkWeatherInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void initialize() {
        //-------------Register table observer----------
        TableObserver observer = new TableObserver(this.getActivity().getApplicationContext(), UIUpdater);
        mContentResolver = getActivity().getContentResolver();
        mContentResolver.registerContentObserver(MusicTrackCommonDataDB.CONTENT_URI, true, observer);
    }

    public void checkWeatherInfo() {
        //------------ check if the weather data is existed and not expired
        String[] projection = {
            MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
            MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Constant.DB_KEY_DAILY_WEATHER) };
        try {
            Cursor cursor = mContentResolver.query(MusicTrackCommonDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
            cursor.moveToFirst();
            String expirationDateString = cursor.getString(cursor.getColumnIndex(MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE));

            SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss ZZZ yyyy");
            Date expirationDate = dateFormat.parse(expirationDateString);

            if (new Date().after(expirationDate)) {
                Log.d("daz should update date", "date is expired");
                manuallyUpdateWeather();
            }
            String JSONContent = cursor.getString(cursor.getColumnIndex(MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
            updateWeatherUI(JSONContent);

            Log.d("daz", "ui got the weather json:" + JSONContent);
        } catch (CursorIndexOutOfBoundsException e){
            Log.d("daz", "ui cannot get the weather json, should start to update immediately");
            manuallyUpdateWeather();
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // should not call sync adapter for manually update?
    private void manuallyUpdateWeather () {
        Log.d("StartFragment", "manually update");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putString(Constant.SYNC_UPDATE, Constant.UPDATE_WEATHER);
        ContentResolver.requestSync(MusicTrackMetaData.mAccount, MusicTrackMetaData.AUTHORITY, bundle);
    }

    private Handler UIUpdater = new Handler() {
        public void handleMessage (Message msg) {
            switch (msg.what) {
                case Constant.UPDATE_START_FRAGMENT_UI:
                    Log.d("daz", "i am going to update UI");
                    Bundle bundle = msg.getData();
                    String JSONContent = bundle.getString(Constant.JSON_CONTENT);
                    Log.d("daz in start fragment", "should show json content:" + JSONContent);
                    updateWeatherUI(JSONContent);
                    break;
                default:

            }
        }
    };

    public void updateWeatherUI (String weatherJSONContent) {
        try {
            JSONObject weatherJSONObject = new JSONObject(weatherJSONContent);
            chanceOfRain.setText(weatherJSONObject.getString("chance-of-rain") + "%");
            uvIndex.setText(weatherJSONObject.getString("uv"));
            startTemperature.setText(weatherJSONObject.getString("maxT") + ".C");
            suggestionDialog.setText(weatherJSONObject.getString("feeling") + "," + weatherJSONObject.getString("condition"));
            city.setText(LocationMetaData.getCurrentAdminArea());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.go_running_button:
                mActivity.startService(mStartMusicServiceIntent);
                startActivity(new Intent(mActivity,RunningActivity.class));
                break;
        }
    }

}
