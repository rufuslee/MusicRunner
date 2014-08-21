package com.amk2.musicrunner.my;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.utilities.RestfulUtility;
import com.amk2.musicrunner.utilities.SharedPreferencesUtility;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MyFragment extends Fragment implements TabHost.OnTabChangeListener, View.OnClickListener {

    public static final String RUNNING_TAB_TAG = "running_tab_tag";
    public static final String MUSIC_TAB_TAG = "music_tab_tag";

    public static class MyTabState {
        public static final int RUNNING = 0;
        public static final int MUSIC = 1;
    }

    public interface MyTabFragmentListener {
        void onSwitchBetweenMyAndPastRecordFragment();
    }

    private Activity mActivity;

    private ContentResolver mContentResolver;

    private MyTabFragmentListener mMyTabFragmentListener;

    private LayoutInflater inflater;
    private LinearLayout myMusicContainer;

    private TabHost mTabHost;
    private ProgressBar timesBar;
    private ProgressBar speedsBar;
    private ProgressBar caloriesBar;
    private ProgressBar distanceBar;
    private Integer timesBarStatus;
    private Integer speedsBarStatus;
    private Integer caloriesBarStatus;
    private Integer distanceBarStatus;
    private ImageButton pastRecordMutton;
    private Handler handler = new Handler();

    public void setMyTabFragmentListener(MyTabFragmentListener listener) {
        mMyTabFragmentListener = listener;
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.my_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
		// This function is like onCreate() in activity.
		// You can start from here.
        SharedPreferences preferences = this.getActivity().getSharedPreferences(Constant.PREFERENCE_NAME, Context.MODE_PRIVATE);
        String account = preferences.getString(Constant.ACCOUNT_PARAMS, null);
//        TextView textView = (TextView) getView().findViewById(R.id.my_view);
//        textView.setText(account,TextView.BufferType.EDITABLE);
        inflater = (LayoutInflater) getView().getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        initialize();
	}

    private void initialize() {
        initTabs();

        mContentResolver = getActivity().getContentResolver();

        timesBar = (ProgressBar) getView().findViewById(R.id.time_bar);
        timesBar.setMax(100);

        speedsBar = (ProgressBar) getView().findViewById(R.id.speeds_bar);
        speedsBar.setMax(100);

        caloriesBar = (ProgressBar) getView().findViewById(R.id.calories_bar);
        caloriesBar.setMax(100);

        distanceBar = (ProgressBar) getView().findViewById(R.id.distance_bar);
        distanceBar.setMax(100);

        pastRecordMutton = (ImageButton) getView().findViewById(R.id.my_past_record_button);
        pastRecordMutton.setOnClickListener(this);

        myMusicContainer = (LinearLayout) getView().findViewById(R.id.my_music_container);

        //showing user name
        String userName = SharedPreferencesUtility.getAccount(getActivity());
        TextView userNameTextView = (TextView) getView().findViewById(R.id.my_user_name);
        userNameTextView.setText(userName);
    }

    @Override
    public void onStart() {
        super.onStart();
        myMusicContainer.removeAllViews();
        //setValueOfProgressBar();
    }

    private void initTabs(){
        mTabHost = (TabHost) getView().findViewById(R.id.my_tab_host);
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        layoutInflater.inflate(R.layout.running_map_tab, null);

        mTabHost.setup();
        addTab(RUNNING_TAB_TAG, "Running");
        addTab(MUSIC_TAB_TAG, "Music");

//        TabHost.TabSpec spec = mTabHost.newTabSpec("Running");
//        spec.setContent(R.id.my_running_tab);
//        spec.setIndicator("Running", getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
//        mTabHost.addTab(spec);
//        TabHost.TabSpec spec2 = mTabHost.newTabSpec("Music");
//        spec2.setContent(R.id.my_music_tab);
//        spec2.setIndicator("Music", getResources().getDrawable(android.R.drawable.ic_lock_idle_alarm));
//        mTabHost.addTab(spec2);

        mTabHost.setCurrentTab(0);

//        //set default tab background color
//        tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.BLACK);
//        tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.WHITE);
//        //set tab text color by selector
//        TabWidget tabWidget = (TabWidget) tabHost.findViewById(android.R.id.tabs);
//        View tabView = tabWidget.getChildTabViewAt(0);
//        TextView tab = (TextView) tabView.findViewById(android.R.id.title);
//        tab.setTextColor(this.getResources().getColorStateList(R.drawable.my_tab_selector));
//        View tabView2 = tabWidget.getChildTabViewAt(1);
//        TextView tab2 = (TextView) tabView2.findViewById(android.R.id.title);
//        tab2.setTextColor(this.getResources().getColorStateList(R.drawable.my_tab_selector));
        //set tab background color depending on selected/unselected
        mTabHost.setOnTabChangedListener(this);
    }

    private void addTab(String tag, String labelText) {
        View tabView = getTabView(tag);
        TextView tabText = (TextView)tabView.findViewById(R.id.tab_text);
        tabText.setText(labelText);
        int contentId = -1;
        if(RUNNING_TAB_TAG.equals(tag)) {
            contentId = R.id.my_running_tab;
        } else if(MUSIC_TAB_TAG.equals(tag)) {
            contentId = R.id.my_music_tab;
        }
        mTabHost.addTab(mTabHost.newTabSpec(tag).setIndicator(tabView)
                .setContent(contentId));
    }

    private View getTabView(String tag) {
        LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
        View tabView = new View(mActivity);
        if(RUNNING_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.my_running_tab, null);
        } else if(MUSIC_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.my_music_tab, null);
        }
        return tabView;
    }

    private String getMyStatus(){
        SharedPreferences preferences = getActivity().getSharedPreferences(Constant.PREFERENCE_NAME, getActivity().MODE_PRIVATE);
        String account =  preferences.getString(Constant.ACCOUNT_PARAMS, null);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(Constant.AWS_HOST + "/getMyStatus");
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("userAccount", account));
        HttpResponse response = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(pairs));
            response = client.execute(post);
        } catch (UnsupportedEncodingException uee) {

        } catch (ClientProtocolException cpe) {
            //ignore this exception for now
        } catch (IOException ioe) {
            //ignore this exception for now
        }
        return RestfulUtility.getStatusCode(response);
    }

    private void setValueOfProgressBar () {
        int times      = 0;
        Double highestSpeed = 0.0;
        Double totalDistance   = 0.0;
        Double totalCalories   = 0.0;
        String distance, calories, speed, songNames;

        String[] projection = {
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED,
                MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SONGS
        };
        Cursor cursor = mContentResolver.query(MusicTrackMetaData.MusicTrackRunningEventDataDB.CONTENT_URI, projection, null, null, null);
        while(cursor.moveToNext()) {
            distance           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_DISTANCE));
            calories           = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_CALORIES));
            speed              = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SPEED));
            songNames          = cursor.getString(cursor.getColumnIndex(MusicTrackMetaData.MusicTrackRunningEventDataDB.COLUMN_NAME_SONGS));

            times++;
            highestSpeed = (Double.parseDouble(speed) > highestSpeed) ? Double.parseDouble(speed) : highestSpeed;
            totalCalories += Double.parseDouble(calories);
            totalDistance += Double.parseDouble(distance);

            addMusicSongs(songNames);
        }
        timesBarStatus = Integer.valueOf(times);
        timesBar.setProgress(timesBarStatus);
        TextView timesTextView = (TextView) getView().findViewById(R.id.my_times_text);
        timesTextView.setText(timesBarStatus.toString());

        speedsBarStatus = Integer.valueOf(highestSpeed.intValue());
        speedsBar.setProgress(speedsBarStatus);
        TextView speedsTextView = (TextView) getView().findViewById(R.id.my_speeds_text);
        speedsTextView.setText(speedsBarStatus.toString());

        caloriesBarStatus = Integer.valueOf(totalCalories.intValue());
        caloriesBar.setProgress(caloriesBarStatus);
        TextView caloriesTextView = (TextView) getView().findViewById(R.id.my_calories_text);
        caloriesTextView.setText(caloriesBarStatus.toString());

        distanceBarStatus = Integer.valueOf(totalDistance.intValue());
        distanceBar.setProgress(distanceBarStatus);
        TextView distanceTextView = (TextView) getView().findViewById(R.id.my_distance_text);
        distanceTextView.setText(distanceBarStatus.toString());
    }

    @Override
    public void onTabChanged(String tabId) {
//        TabHost tab = (TabHost) getView().findViewById(R.id.my_tab_host);
//        for (int i = 0; i < tab.getTabWidget().getChildCount(); i++) {
//            tab.getTabWidget().getChildAt(i)
//                    .setBackgroundResource(R.color.my_background_tab_unselected); // unselected
//        }
//        tab.getTabWidget().getChildAt(tab.getCurrentTab())
//                .setBackgroundResource(R.color.my_background_tab_selected); // selected
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_past_record_button:
                // Go to past record fragment
                mMyTabFragmentListener.onSwitchBetweenMyAndPastRecordFragment();
                break;
        }
    }

    public void addMusicSongs (String songNames) {
        String songName = getMostEfficientSongs(songNames);
        if (songName.length() > 0) {
            View myMusicTemplate = inflater.inflate(R.layout.my_music_template, null);
            TextView textViewSongName = (TextView) myMusicTemplate.findViewById(R.id.song_name);
            textViewSongName.setText(songName);

            myMusicContainer.addView(myMusicTemplate);
        }

    }

    public static String getMostEfficientSongs (String songNames) {
        String mostEfficientSong = "";
        String result = "";
        Double mostEfficientPerformance = -1.0;
        for (String song : songNames.split(Constant.SONG_SEPARATOR)) {
            if (song.length() > 0) {
                String[] songXperf = song.split(Constant.PERF_SEPARATOR);
                Double perf = Double.parseDouble(songXperf[1]);
                if (perf > mostEfficientPerformance) {
                    mostEfficientSong = songXperf[0];
                    mostEfficientPerformance = perf;
                }
            }
        }
        if (mostEfficientSong.length() > 0) {
            result = mostEfficientSong + "   " + mostEfficientPerformance.toString() + " kcal/min";
        }

        Log.d("getMostEfficientSongs", result);
        return result;
    }
}
