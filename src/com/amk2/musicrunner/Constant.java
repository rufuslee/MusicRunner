package com.amk2.musicrunner;

/**
 * Created by ktlee on 5/25/14.
 */
public class Constant {
    public static final boolean isServerOn = false;

    //main activity
    public static final long MILLISECONDS_PER_SECOND = 1L;
    public static final long ONE_MINUTE = 60L;
    public static final String SYNC_UPDATE = "com.amk2.musicrunner.update";
    public static final String SYNC_CITYCODE = "com.amk2.musicrunner.citycode";

    public static final String UPDATE_WEATHER = "com.amk2.musicrunner.updateweather";
    public static final String UPDATE_24HRS_WEATHER = "com.amk2.musicrunner.update24hrsweather";
    public static final String UPDATE_WEEKLY_WEATHER = "com.amk2.musicrunner.updateweeklyweather";
    public static final String UPDATE_UBIKE = "com.amk2.musicrunner.updateubike";

    public static final int EXPIRATION_DATE_DURATION_DAILY  = 1;
    public static final int EXPIRATION_DATE_DURATION_24HRS  = 1;
    public static final int EXPIRATION_DATE_DURATION_WEEKLY = 12;

    public static final String JSON_CONTENT = "com.amk2.musicrunner.updateweather.jsoncontent";
    public static final String EXPIRATION_DATE = "com.amk2.musicrunner.updateweather.expirationdate";

    public static final String DB_KEY_DAILY_WEATHER = "com.amk2.musicrunner.daily.weather";
    public static final String DB_KEY_24HRS_WEATHER = "com.amk2.musicrunner.24hrs.weather";
    public static final String DB_KEY_WEEKLY_WEATHER = "com.amk2.musicrunner.weekly.weather";
    public static final String DB_KEY_YOUBIKE = "com.amk2.musicrunner.youbike";

    // Server API
    public static final String HOST = "http://ec2-54-186-18-12.us-west-2.compute.amazonaws.com";
    public static final String PORT = ":8080";
    public static final String baseWeatherUrlString = HOST + PORT + "/weatherJSON";//TODO need to append city query behind
    public static final String baseWeatherWeekUrlString = HOST + PORT + "/weatherWeekJSON";//TODO need to append city query behind
    public static final String baseWeather24HoursUrlString = HOST + PORT + "/weather24HoursJSON";//TODO need to append city query behind
    public static final String storeRunningEventUrlString = HOST + PORT + "store?type=event";//TODO need to append city query behind
    public static final String cityCodeQuery = "cityCode=";

    public static final int UPDATE_START_FRAGMENT_UI = 1;

}
