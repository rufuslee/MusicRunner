package com.amk2.musicrunner.start;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by daz on 2014/4/27.
 */
public class NetworkAccess {
    public static final String baseWeatherUrlString = "http://192.168.1.18:3000/weatherJSON";//TODO need to append city query behind
    //make http request to download data from server
    public InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        Log.d("weather url", urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }
}
