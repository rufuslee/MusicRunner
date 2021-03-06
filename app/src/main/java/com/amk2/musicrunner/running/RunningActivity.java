package com.amk2.musicrunner.running;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.Toast;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.RunningTabContentFactory;
import com.amk2.musicrunner.finish.FinishRunningActivity;
import com.amk2.musicrunner.main.AbstractTabViewPagerAdapter;
import com.amk2.musicrunner.utilities.PhotoLib;
import com.amk2.musicrunner.utilities.ShowImageActivity;
import com.amk2.musicrunner.utilities.StringLib;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ktlee on 5/10/14.
 */
public class RunningActivity extends Activity implements TabHost.OnTabChangeListener,
        ViewPager.OnPageChangeListener, View.OnClickListener, MusicFragment.OnChangeSongListener {

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
    private MapFragmentRun mMapFragment;
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
    private Integer previousSongStartTime    = 0;
    private Double previousSongStartCalories = 0.0;

    private Double mockDistance = 0.0;

    private Double distance = 0.0;
    private Double calorie = 0.0;
    private Double running_speed = 0.0;

    private String distanceString;
    private String calorieString;
    private String speedString = "0";
    private String songNames = "";

    private String photoPath;

    private NotificationCenter notificationCenter;

    private File musicRunnerDir;

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
        addTab(MAP_TAB_TAG, getString(R.string.map_tab_text));
        addTab(MUSIC_TAB_TAG, getString(R.string.music_tab_text));
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
        mMapFragment = (MapFragmentRun)mFragmentManager.findFragmentByTag(RunningFragmentTag.MAP_FRAGMENT_TAG);
        if(mMapFragment == null) {
            mMapFragment = new MapFragmentRun();
            transaction.add(R.id.running_view_pager, mMapFragment, RunningFragmentTag.MAP_FRAGMENT_TAG);
        }

        // Init MusicFragment
        mMusicFragment = (MusicFragment)mFragmentManager.findFragmentByTag(RunningFragmentTag.MUSIC_FRAGMENT_TAG);
        if(mMusicFragment == null) {
            mMusicFragment = new MusicFragment();
            transaction.add(R.id.running_view_pager, mMusicFragment, RunningFragmentTag.MUSIC_FRAGMENT_TAG);
        }
        mMusicFragment.setOnChangeSongListener(this);

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

        musicRunnerDir = getAlbumStorageDir(Constant.Album);

        picPreview.setOnClickListener(this);
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
                    //distance += 0.01;
                    distance = MapFragmentRun.getmTotalDistance() * 0.001;
                    distanceString = distance.toString();
                    distanceString = StringLib.truncateDoubleString(distanceString, 2);
                    runningDistance.setText(distanceString);

                    //update calorie
                    //calorie += 0.1;
                    //mockDistance += 2.41;
                    calorie = calculateCalories(totalSec, MapFragmentRun.getmTotalDistance());
                    calorieString = calorie.toString();
                    calorieString = StringLib.truncateDoubleString(calorieString, 2);
                    runningCalorie.setText(calorieString);

                    //update ratio
                    //running_speed += 0.01;
                    if (distance > 0) {
                        running_speed = distance/ ((double) totalSec / 60);//MapFragmentRun.getmSpeed();
                    }
                    speedString = running_speed.toString();
                    speedString = StringLib.truncateDoubleString(speedString, 2);
                    runningSpeedRatio.setText(speedString);

                    if (actualSec % Constant.ONE_MINUTE == 0) {
                        notificationCenter.notifyStatus(actualMin, actualSec, distance, running_speed, calorie);
                    }
                    break;
            }
        }
    };

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    private Double calculateCalories (int timeInSec, Double distanceInMeter) {
        if (distanceInMeter == 0.0) {
            return 0.0;
        }
        double mins  = (double) timeInSec / 60;
        double hours = (double) timeInSec / 3600;
        double per400meters = distanceInMeter / 400;
        double speed = mins / per400meters;
        double K = 30 / speed;
        double calories = 70*hours*K;

        return calories;
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == REQUEST_IMAGE_CAPTURE && resCode == RESULT_OK) {
            galleryAddPic();
            Bitmap resizedPhoto = PhotoLib.resizeToFitTarget(photoPath, picPreview.getLayoutParams().width, picPreview.getLayoutParams().height);
            picPreview.setImageBitmap(resizedPhoto);
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
        stopService(new Intent(this,MusicService.class));
        runningTask.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_runner, menu);
        return false;
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
                onChangeMusicSong(mMusicFragment.getLastMusicRecord());  // Handle the last song
                stopService(new Intent(this,MusicService.class));
                finish();

                Intent finishRunningIntent = new Intent(getApplication(), FinishRunningActivity.class);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_DURATION, totalSec);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_DISTANCE, distanceString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_CALORIES, calorieString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_SPEED, speedString);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_PHOTO, photoPath);
                finishRunningIntent.putExtra(FinishRunningActivity.FINISH_RUNNING_SONGS, songNames);
                startActivity(finishRunningIntent);
                break;
            case R.id.pause_running:
                if (isRunning) {
                    pauseButton.setText("Resume");
                    pauseButton.setBackgroundColor(getResources().getColor(R.color.running_button_resume));
                } else {
                    pauseButton.setText("Pause");
                    pauseButton.setBackgroundColor(getResources().getColor(R.color.running_button_pause));
                }
                isRunning = !isRunning;
                break;
            case R.id.camera:
                dispatchTakePictureIntent();
                break;
            case R.id.pic_preview:
                if (photoPath != null) {
                    Intent intent = new Intent(this, ShowImageActivity.class);
                    intent.putExtra(ShowImageActivity.PHOTO_PATH, photoPath);
                    startActivity(intent);
                }
                break;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", musicRunnerDir);
        photoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + photoPath)));
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs() || file.isDirectory()) {
            Log.e("Album", "Directory not created");
        }
        return file;
    }

    @Override
    public void onChangeMusicSong(MusicRecord previousRecord) {
        String songName = previousRecord.mMusicSong.mTitle;
        String performanceString;
        Double timeDiff = ((double)totalSec - previousSongStartTime.doubleValue()) / 60;
        Double caloriesDiff = calorie - previousSongStartCalories;
        Double performance = 0.0;
        if (timeDiff != 0) {
            performance = caloriesDiff/timeDiff;
        }
        performanceString = StringLib.truncateDoubleString(performance.toString(), 2);
        songNames += (songName + Constant.PERF_SEPARATOR + performanceString + Constant.SONG_SEPARATOR );
        Log.d("song", songName + ", " + performanceString);
        previousSongStartCalories = calorie;
        previousSongStartTime     = totalSec;

        mMapFragment.musicChangeCallback(previousRecord);

        //Log.d("danny","Previous music title = " + previousRecord.mMusicSong.mTitle);
        //Log.d("danny","Previous music playing duration = " + previousRecord.mPlayingDuration);
    }
}
