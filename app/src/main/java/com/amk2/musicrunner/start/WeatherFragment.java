package com.amk2.musicrunner.start;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.start.StartFragment.StartTabFragmentListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * Created by daz on 2014/4/29.
 */
public class WeatherFragment extends Fragment{
    private static final LinearLayout.LayoutParams weeklyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);

    private String sunnyUri     = "@drawable/sunny";
    private String cloudyUri    = "@drawable/cloudy";
    private String rainUri      = "@drawable/rainy";
    private String heavyrainUri = "@drawable/heavilyrain";

    private String weeklySunnyUri  = "@drawable/weather_sunny";
    private String weeklyHotUri    = "@drawable/weather_hot";
    private String weeklyColdUri   = "@drawable/weather_cold";
    private String weeklyRainyUri  = "@drawable/weather_rainy";
    private String weeklyCloudyUri = "@drawable/weather_cloudy";

    HashMap<String, Integer> condIndex = new HashMap<String, Integer>();
    HashMap<String, Integer> weeklyCondIndex = new HashMap<String, Integer>();

    private TextView weatherTemp;
    private TextView weatherSummary;
    private LinearLayout hourlyWeatherForecast;
    private LinearLayout weeklyWeatherForecast;
    private GetAddressFromLocation getAddress;
    private LayoutInflater inflater;
    private Location location;
    private StartTabFragmentListener mStartTabFragmentListener;

    private ContentResolver mContentResolver;

    public void setStartTabFragmentListener(StartTabFragmentListener listener) {
    	mStartTabFragmentListener = listener;
    }

    public void onBackPressed() {
    	mStartTabFragmentListener.onSwitchBetweenStartAndWeatherFragment();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAddress = new GetAddressFromLocation(this.getActivity().getApplicationContext());
        mContentResolver = this.getActivity().getContentResolver();

        int sunnyRes       = getResources().getIdentifier(sunnyUri, null, this.getActivity().getPackageName());
        int cloudyRes      = getResources().getIdentifier(cloudyUri, null, this.getActivity().getPackageName());
        int rainyRes       = getResources().getIdentifier(rainUri, null, this.getActivity().getPackageName());
        int heavilyrainRes = getResources().getIdentifier(heavyrainUri, null, this.getActivity().getPackageName());

        int weeklySunnyRes  = getResources().getIdentifier(weeklySunnyUri, null, this.getActivity().getPackageName());
        int weeklyColdRes   = getResources().getIdentifier(weeklyColdUri, null, this.getActivity().getPackageName());
        int weeklyHotRes    = getResources().getIdentifier(weeklyHotUri, null, this.getActivity().getPackageName());
        int weeklyRainyRes  = getResources().getIdentifier(weeklyRainyUri, null, this.getActivity().getPackageName());
        int weeklyCloudyRes = getResources().getIdentifier(weeklyCloudyUri, null, this.getActivity().getPackageName());

        condIndex.put("sunny", sunnyRes);
        condIndex.put("cloudy", cloudyRes);
        condIndex.put("rainy", rainyRes);
        condIndex.put("heavilyrain", heavilyrainRes);

        weeklyCondIndex.put("sunny",  weeklySunnyRes);
        weeklyCondIndex.put("cold",   weeklyColdRes);
        weeklyCondIndex.put("hot",    weeklyHotRes);
        weeklyCondIndex.put("rainy",  weeklyRainyRes);
        weeklyCondIndex.put("cloudy", weeklyCloudyRes);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weather_fragment, container, false);
    }

    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        View thisView = getView();
        /*hourlyWeatherForecast = (LinearLayout) thisView.findViewById(R.id.hourly_weather_forecast);
        weeklyWeatherForecast = (LinearLayout) thisView.findViewById(R.id.weekly_weather_forecast);
        inflater = (LayoutInflater) thisView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        weatherTemp    = (TextView) thisView.findViewById(R.id.weather_temp);
        weatherSummary = (TextView) thisView.findViewById(R.id.weather_summary);*/
/*
        try {
            updateWeatherSummary();
            updateWeeklyForecast();
            update24HoursForecast();
        } catch (CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void updateWeatherSummary () {
        // TODO projection could be moved to a static place
        String[] projection = {
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Constant.DB_KEY_DAILY_WEATHER) };

        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackCommonDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        String JSONContent = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
        updateWeatherSummary(JSONContent);
    }

    private void updateWeatherSummary (String JSONContent) {
        try {
            JSONObject weatherJSONObject = new JSONObject(JSONContent);
            weatherTemp.setText(weatherJSONObject.getString("maxT") + ".C");
            weatherSummary.setText(weatherJSONObject.getString("feeling") + "," + weatherJSONObject.getString("condition"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateWeeklyForecast () {
        // TODO projection could be moved to a static place
        String[] projection = {
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Constant.DB_KEY_WEEKLY_WEATHER) };

        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackCommonDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        String JSONContent = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
        updateWeeklyForecastUI(JSONContent);

        Log.d("daz", "ui got the weather json:" + JSONContent);
    }
    private void updateWeeklyForecastUI(String JSONContent) {
        try {
            JSONArray weeklyJSONArray = new JSONArray(JSONContent);
            int length = weeklyJSONArray.length();
            for (int i = 0; i < length; i++) {
                addWeeklyForecast(weeklyJSONArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void addWeeklyForecast (JSONObject weeklyJSONObject) throws JSONException {
        View weekly = inflater.inflate(R.layout.weekly_template, null);
        TextView day = (TextView) weekly.findViewById(R.id.day);
        TextView temperature = (TextView) weekly.findViewById(R.id.temperature);
        ImageView forecastGraph = (ImageView) weekly.findViewById(R.id.forecast_graph);

        try {
            day.setText(DayMapping.getDay(weeklyJSONObject.getString("day")));
            temperature.setText(weeklyJSONObject.getString("maxT") + "/" + weeklyJSONObject.getString("minT") + ".C");
            Drawable res = getResources().getDrawable(weeklyCondIndex.get(weeklyJSONObject.getString("condIndex")));
            forecastGraph.setImageDrawable(res);
            weeklyWeatherForecast.addView(weekly, weeklyParams);
        } catch (NullPointerException e) {
            Log.d("daz in weekly weather condindex=", weeklyJSONObject.getString("condIndex").toString());
        }
    }

    private void update24HoursForecast () {
        // TODO projection could be moved to a static place
        String[] projection = {
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT,
                MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_EXPIRATION_DATE
        };
        String selection = MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_DATA_TYPE + " LIKE ?";
        String[] selectionArgs = { String.valueOf(Constant.DB_KEY_24HRS_WEATHER) };

        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackCommonDataDB.CONTENT_URI, projection, selection, selectionArgs, null);
        cursor.moveToFirst();
        String JSONContent = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackCommonDataDB.COLUMN_NAME_JSON_CONTENT));
        update24HoursForecastUI(JSONContent);

        Log.d("daz", "ui got the weather json:" + JSONContent);
    }

    private void update24HoursForecastUI(String JSONContent) {
        try {
            JSONArray t24HoursJSONArray = new JSONArray(JSONContent);
            int length = t24HoursJSONArray.length();
            for (int i = 0; i < length; i++) {
                add24HoursForecast(t24HoursJSONArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void add24HoursForecast (JSONObject t24HoursJSONArray) throws JSONException {
        View hourly = inflater.inflate(R.layout.hourly_template, null);
        TextView time = (TextView) hourly.findViewById(R.id.time);
        time.setText(t24HoursJSONArray.getString("time") + ":00");
        ImageView weatherGraph = (ImageView) hourly.findViewById(R.id.weather_hourly_icon);

        Log.d("daz in weather condindex=", t24HoursJSONArray.getString("condIndex").toString());
        try {
            Drawable res = getResources().getDrawable(condIndex.get(t24HoursJSONArray.getString("condIndex")));
            weatherGraph.setImageDrawable(res);
            TextView temperature = (TextView) hourly.findViewById(R.id.temperature);
            temperature.setText(t24HoursJSONArray.getString("maxT") + "/" + t24HoursJSONArray.getString("minT") + ".C");
            hourlyWeatherForecast.addView(hourly);
        } catch (NullPointerException e) {
            Log.d("daz in weather condindex=", t24HoursJSONArray.getString("condIndex").toString());
        }
    }

    @Override
    public void onPause () {
        super.onPause();
    }

}
