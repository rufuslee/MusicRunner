package com.amk2.musicrunner.utilities;

import android.os.StrictMode;
import android.util.Log;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.start.WeatherModel;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

/**
 * Created by paulou1009 on 6/19/14.
 */
public class RestfulUtility {

    public static final String LOGIN_ENDPOINT = "/login";
    public static final String REGISTER_ENDPOINT = "/register";
    public static final String GET_SETTING_INFO = "/getSettingInfo";
    public static final String UPDATE_SETTINGS = "/updateSettings";
    public static final String FACEBOOK_LOGIN = "/facebookLogin";

    //extract response content
    public static String getStatusCode(HttpResponse response){
        if(response == null)
            return null;
        StringBuilder sb = new StringBuilder();
        String line = "";
        try{
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException io){

        }
        return sb.toString();
    }

    public static HttpResponse restfulPostRequest(String endpoint, List<NameValuePair> pairs) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 3000);
        HttpClient client = new DefaultHttpClient(httpParams);
        HttpPost post = new HttpPost(Constant.AWS_HOST + endpoint);

        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(pairs));
            response = client.execute(post);

        } catch (ConnectTimeoutException cte) {
            Log.e("connection timeout ", "time out !!!!");
        } catch (UnsupportedEncodingException uee) {

        } catch (ClientProtocolException cpe) {
            //ignore this exception for now
        } catch (IOException ioe) {
            //ignore this exception for now
        }
        return response;
    }

    public static InputStream restfulGetRequest (String urlString) {
        InputStream inputStream;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int responseCode = conn.getResponseCode();
            Log.d("Restful api", "Response Code is : " + responseCode);
            inputStream = conn.getInputStream();
            return inputStream;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getStringFromInputStream(InputStream in) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            String s;
            br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            while ( (s = br.readLine()) != null) {
                sb.append(s);
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("Error","Unsupport UTF-8 data type");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
