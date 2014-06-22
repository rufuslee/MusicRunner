package com.amk2.musicrunner.running;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.google.android.maps.MapActivity;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.RunningTabContentFactory;
import com.amk2.musicrunner.finish.FinishRunningActivity;
import com.amk2.musicrunner.main.AbstractTabViewPagerAdapter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ktlee on 5/10/14.
 */
public class RunningActivity extends MapActivity implements TabHost.OnTabChangeListener,
        ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final int STATE_RUNNING = 1;
    public static final int TAB_SIZE = 2;
    public static final String MAP_TAB_TAG = "map_tab_tag";
    public static final String MUSIC_TAB_TAG = "music_tab_tag";

    public static class RunningFragmentTag {
        public static final String MAP_FRAGMENT_TAG = "map_fragment_tag";
        public static final String MUSIC_FRAGMENT_TAG = "music_fragment_tag";
    }

    public static class RunningTabState {
        public static final int MAP = 0;
        public static final int MUSIC = 1;
    }

    private FragmentManager mFragmentManager;

    private TabHost mTabHost;
    private ViewPager mRunningViewPager;
    private RunningTabViewPagerAdapter mRunningTabViewPagerAdapter;
    private MapFragment mMapFragment;
    private MusicFragment mMusicFragment;

    private TextView runningDistance;
    private TextView runningCalorie;
    private TextView runningSpeedRatio;
    private TextView hour;
    private TextView min;
    private TextView sec;

    private Button pauseButton;
    private Button mStopButton;

    private ImageView picPreview;
    private ImageButton camera;

    private boolean isRunning = false;
    private int totalSec   = 0;
    private int actualSec  = 0;
    private int actualMin  = 0;
    private int actualHour = 0;

    private Double distance = 0.0;
    private Double calorie = 0.0;
    private Double running_ratio = 0.0;

    private String distanceString;
    private String calorieString;
    private String ratioString;

    private NotificationCenter notificationCenter;

    public class RunningTabViewPagerAdapter extends AbstractTabViewPagerAdapter {

        public RunningTabViewPagerAdapter(FragmentManager fm, int size) {
            super(fm, size);
        }

        @Override
        protected Fragment getFragment(int position) {
            switch(position) {
            case RunningTabState.MAP:
                return mMapFragment;
            case RunningTabState.MUSIC:
                return mMusicFragment;
            }
            return null;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running);
        initialize();
    }

    private void initialize() {
        findViews();
        initTabs();
        initFragments();
        initViewPager();
        Timer timer = new Timer();
        timer.schedule(runningTask, 0, 1000);
        notificationCenter = new NotificationCenter(this);
    }

    private void initViewPager() {
        mRunningTabViewPagerAdapter = new RunningTabViewPagerAdapter(mFragmentManager,TAB_SIZE);
        mRunningViewPager.setAdapter(mRunningTabViewPagerAdapter);
        mRunningViewPager.setOnPageChangeListener(this);
    }

    private void initTabs() {
        mTabHost.setup();
        addTab(MAP_TAB_TAG,getString(R.string.map_tab_text));
        addTab(MUSIC_TAB_TAG,getString(R.string.music_tab_text));
        mTabHost.setOnTabChangedListener(this);
    }

    private void addTab(String tag, String labelText) {
        View tabView = getTabView(tag);
        TextView tabText = (TextView)tabView.findViewById(R.id.tab_text);
        tabText.setText(labelText);
        mTabHost.addTab(mTabHost.newTabSpec(tag).setIndicator(tabView)
                .setContent(new RunningTabContentFactory(this)));
    }

    private View getTabView(String tag) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View tabView = new View(this);
        if(MAP_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.running_map_tab, null);
        } else if(MUSIC_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.running_music_tab, null);
        }
        return tabView;
    }

    private void initFragments() {
        mFragmentManager = getFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        // Init MapFragment
        mMapFragment = (MapFragment)mFragmentManager.findFragmentByTag(RunningFragmentTag.MAP_FRAGMENT_TAG);
        if(mMapFragment == null) {
            mMapFragment = new MapFragment();
            transaction.add(R.id.running_view_pager, mMapFragment, RunningFragmentTag.MAP_FRAGMENT_TAG);
        }

        // Init MusicFragment
        mMusicFragment = (MusicFragment)mFragmentManager.findFragmentByTag(RunningFragmentTag.MUSIC_FRAGMENT_TAG);
        if(mMusicFragment == null) {
            mMusicFragment = new MusicFragment();
            transaction.add(R.id.running_view_pager, mMusicFragment, RunningFragmentTag.MUSIC_FRAGMENT_TAG);
        }

        transaction.hide(mMapFragment);
        transaction.hide(mMusicFragment);

        transaction.commit();
    }


    private void findViews() {
        hour = (TextView) findViewById(R.id.timer_hour);
        min = (TextView) findViewById(R.id.timer_minute);
        sec = (TextView) findViewById(R.id.timer_second);

        runningDistance = (TextView) findViewById(R.id.running_distance);
        runningCalorie = (TextView) findViewById(R.id.running_calorie);
        runningSpeedRatio = (TextView) findViewById(R.id.running_speed_ratio);

        pauseButton = (Button) findViewById(R.id.pause_running);
        pauseButton.setOnClickListener(this);
        mStopButton = (Button)findViewById(R.id.stop_running);
        mStopButton.setOnClickListener(this);

        picPreview = (ImageView) findViewById(R.id.pic_preview);
        camera = (ImageButton) findViewById(R.id.camera);
        camera.setOnClickListener(this);

        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mRunningViewPager = (ViewPager)findViewById(R.id.running_view_pager);
    }

    private TimerTask runningTask = new TimerTask() {
        @Override
        public void run() {
            if (isRunning) {
                totalSec ++;
                Message msg = new Message();
                msg.what = STATE_RUNNING;
                handler.sendMessage(msg);
            }
        }
    };

    private Handler handler = new Handler(){
        public void handleMessage (Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case STATE_RUNNING:

                    // update time
                    actualSec  = totalSec%60;

                    if (actualSec < 10) {
                        sec.setText("0" + actualSec);
                    } else {
                        sec.setText("" + actualSec);
                    }

                    if (actualSec == 0) {
                        actualMin = (actualMin + 1) % 60;

                        if (actualMin < 10) {
                            min.setText("0" + actualMin);
                        } else {
                            min.setText("" + actualMin);
                        }

                        if (actualMin == 0) {
                            actualHour += 1;

                            if (actualHour < 10) {
                                hour.setText("0" + actualHour);
                            } else {
                                hour.setText("" + actualHour);
                            }
                        }
                    }

                    //update distance
                    distance += 0.01;
                    distanceString = distance.toString();
                    distanceString = truncateDoubleString(distanceString, 2);
                    runningDistance.setText(distanceString);

                    //update calorie
                    calorie += 0.1;
                    calorieString = calorie.toString();
                    calorieString = truncateDoubleString(calorieString, 1);
                    runningCalorie.setText(calorieString);

                    //update ratio
                    running_ratio += 0.01;
                    ratioString = running_ratio.toString();
                    ratioString = truncateDoubleString(ratioString, 2);
                    runningSpeedRatio.setText(ratioString);

                    if (actualSec % 20 == 0) {
                        Log.v("Timer notifies per 10 sec", "lool");
                        notificationCenter.notifyStatus(actualSec, actualSec, distance, running_ratio);
                    }
                    break;
            }
        }
    };

    /*
     * truncateDoubleString: truncate double number to 小數點後兩位
     * str: string of double number
     * allowedDigits: 小數點後幾位
     */
    public static String truncateDoubleString (String str, int allowedDigits) {
        int dot_position = str.indexOf(".");
        if (str.length() - dot_position > (allowedDigits + 1)) { //小數點後數字大於兩位
            Log.d("truncate", "truncate");
            str = str.substring(0, dot_position + allowedDigits + 1);
        }
        return str;
    };

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == REQUEST_IMAGE_CAPTURE && resCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            picPreview.setImageBitmap(imageBitmap);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart () {
        super.onStart();
        isRunning = true;
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
        runningTask.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_runner, menu);
        return true;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onPageSelected(int pos) {
        mTabHost.setCurrentTab(pos);
    }

    @Override
    public void onTabChanged(String tabId) {
        mRunningViewPager.setCurrentItem(mTabHost.getCurrentTab());
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.stop_running:
                stopService(new Intent(this,MusicService.class));
                finish();

                Intent finishRunningIntent = new Intent(getApplication(), FinishRunningActivity.class);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_DISTANCE, distanceString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_CALORIES, calorieString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_SPEED, ratioString);
                startActivity(finishRunningIntent);
                break;
            case R.id.pause_running:
                isRunning = !isRunning;
                break;
            case R.id.camera:
                dispatchTakePictureIntent();
                break;
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
