package com.amk2.musicrunner.weather;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.utilities.RestfulUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by ktlee on 8/10/14.
 */
public class WeatherFragment extends Fragment {

    TextView mDialogTextView;
    TextView mTemperatureTextView;
    TextView mWeatherIconDescTextView;
    TextView mHumidityTextView;
    TextView mUVIndexTextView;
    TextView mWindTextView;

    ImageView mWeatherIconImageView;

    LinearLayout mHourlyForecastContainer;
    LinearLayout m5DaysForecastContainer;
    LayoutInflater inflater;

    private static final LinearLayout.LayoutParams fivedaysParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.weather_fragment, container, false);
    }

    @Override
    public void onActivityCreated (Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        View thisView = getView();
        mDialogTextView          = (TextView) thisView.findViewById(R.id.weather_condition_dialog);
        mTemperatureTextView     = (TextView) thisView.findViewById(R.id.weather_condition_temperature);
        mWeatherIconDescTextView = (TextView) thisView.findViewById(R.id.weather_condition_icon_desc);
        mHumidityTextView        = (TextView) thisView.findViewById(R.id.weather_condition_humidity);
        mUVIndexTextView         = (TextView) thisView.findViewById(R.id.weather_condition_uv);
        mWindTextView            = (TextView) thisView.findViewById(R.id.weather_condition_wind);

        mWeatherIconImageView    = (ImageView) thisView.findViewById(R.id.weather_condition_icon);
        mHourlyForecastContainer = (LinearLayout) thisView.findViewById(R.id.weather_condition_hourly);
        m5DaysForecastContainer  = (LinearLayout) thisView.findViewById(R.id.weather_condition_5days);

        inflater = (LayoutInflater) thisView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        updateWeatherCondition();
        updateWeatherHourly();
        updateWeather5Days();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void updateWeatherCondition() {
        InputStream inputStream = RestfulUtility.restfulGetRequest(Constant.WEATHER_CONDITION_API_URL + "?city=taipei&country=tw");
        String weatherJSONString = RestfulUtility.getStringFromInputStream(inputStream);
        try {
            int iconId;
            JSONObject weatherJSONObject = new JSONObject(weatherJSONString);
            mTemperatureTextView.setText(weatherJSONObject.getString("feelslike_c") + " c");
            mWeatherIconDescTextView.setText(weatherJSONObject.getString("condition"));
            mHumidityTextView.setText(weatherJSONObject.getString("relative_humidity"));
            mUVIndexTextView.setText(weatherJSONObject.getString("UV"));
            mWindTextView.setText(weatherJSONObject.getString("wind_kph") + " mph");

            if (weatherJSONObject.getString("icon_url").matches(".*nt_.*")) {
                iconId = getResources().getIdentifier("nt_" + weatherJSONObject.getString("icon"), "drawable", getActivity().getPackageName());
            } else {
                iconId = getResources().getIdentifier(weatherJSONObject.getString("icon"), "drawable", getActivity().getPackageName());
            }

            Bitmap icon = BitmapFactory.decodeResource(getResources(), iconId);
            mWeatherIconImageView.setImageBitmap(Bitmap.createScaledBitmap(icon, 100, 100, false));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateWeatherHourly () {
        InputStream inputStream = RestfulUtility.restfulGetRequest(Constant.WEATHER_HOURLY_API_URL + "?city=taipei&country=tw");
        String weatherJSONString = RestfulUtility.getStringFromInputStream(inputStream);
        try {
            JSONArray weatherJSONArray = new JSONArray(weatherJSONString);
            int length = weatherJSONArray.length();
            for (int i = 0; i < length; i ++) {
                updateWeatherHourlyUI(weatherJSONArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateWeatherHourlyUI (JSONObject weatherJSONObject) {
        View hourly = inflater.inflate(R.layout.hourly_template, null);
        TextView time         = (TextView) hourly.findViewById(R.id.time);
        ImageView weatherIcon = (ImageView) hourly.findViewById(R.id.weather_hourly_icon);
        TextView temperature  = (TextView) hourly.findViewById(R.id.temperature);

        try {
            int hour = Integer.parseInt(weatherJSONObject.getString("time"));
            int iconId;

            String ampm = weatherJSONObject.getString("ampm");
            if (ampm == "PM") {
                hour -= 12;
            }
            time.setText(hour + " " + ampm);

            if (weatherJSONObject.getString("icon_url").matches(".*nt_.*")) {
                iconId = getResources().getIdentifier("nt_" + weatherJSONObject.getString("icon"), "drawable", getActivity().getPackageName());
            } else {
                iconId = getResources().getIdentifier(weatherJSONObject.getString("icon"), "drawable", getActivity().getPackageName());
            }

            Bitmap icon = BitmapFactory.decodeResource(getResources(), iconId);
            weatherIcon.setImageBitmap(Bitmap.createScaledBitmap(icon, 100, 100, false));
            temperature.setText(weatherJSONObject.getString("temp_c") + " c");

            mHourlyForecastContainer.addView(hourly);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateWeather5Days () {
        InputStream inputStream = RestfulUtility.restfulGetRequest(Constant.WEATHER_5DAYS_API_URL + "?city=taipei&country=tw");
        String weatherJSONString = RestfulUtility.getStringFromInputStream(inputStream);
        try {
            JSONArray weatherJSONArray = new JSONArray(weatherJSONString);
            int length = weatherJSONArray.length();
            for (int i = 0; i < length; i ++) {
                updateWeather5DaysUI(weatherJSONArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateWeather5DaysUI (JSONObject weatherJSONObject) {
        View fivedays = inflater.inflate(R.layout.fivedays_template, null);
        TextView weekday      = (TextView) fivedays.findViewById(R.id.weekday);
        ImageView weatherIcon = (ImageView) fivedays.findViewById(R.id.weather_5days_icon);
        Drawable res;
        try {
            Log.d("daz", weatherJSONObject.getString("icon"));
            weekday.setText(weatherJSONObject.getString("weekday").substring(0,3));
            if (weatherJSONObject.getString("icon").equals("cloudy") ||
                weatherJSONObject.getString("icon").equals("mostlycloudy") ||
                weatherJSONObject.getString("icon").equals("partlycloudy")) { // cloudy

                res = getResources().getDrawable(R.drawable.weather_cloudy);
            } else if (weatherJSONObject.getString("icon").equals("clear") ||
                       weatherJSONObject.getString("icon").equals("partlysunny")){ // sunny

                res = getResources().getDrawable(R.drawable.weather_sunny);
            } else if (weatherJSONObject.getString("icon").equals("chancerain") ||
                       weatherJSONObject.getString("icon").equals("chancetstorms") ||
                       weatherJSONObject.getString("icon").equals("rain") ||
                       weatherJSONObject.getString("icon").equals("tstorms")) { // rainy

                res = getResources().getDrawable(R.drawable.weather_rainy);
            } else if (weatherJSONObject.getString("icon").equals("sunny")) { // hot

                res = getResources().getDrawable(R.drawable.weather_hot);
            } else { // default cloudy

                res = getResources().getDrawable(R.drawable.weather_cloudy);
            }
            weatherIcon.setImageDrawable(res);
            fivedaysParams.setMargins(0,0,2,0);
            m5DaysForecastContainer.addView(fivedays, fivedaysParams);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

    }
}
