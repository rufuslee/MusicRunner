
package com.amk2.musicrunner.main;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.services.SyncService;
import com.amk2.musicrunner.sqliteDB.MusicTrackMetaData;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.running.MusicService;
import com.amk2.musicrunner.running.RunningActivity;
import com.amk2.musicrunner.location.LocationHelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

/**
 * Main activity of MusicRunner+
 * 
 * @author DannyLin
 */
public class MusicRunnerActivity extends Activity {

    private UIController mUIController;
    private ContentResolver mContentResolver;
    private LocationHelper mLocationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_runner);
        initialize();
        //initializeSyncJobs();
        //initializeLocation();
        mUIController.onActivityCreate(savedInstanceState);
        goToRunningPage();
    }

    private void goToRunningPage() {
        if (isMyServiceRunning(MusicService.class)) {
            startActivity(new Intent(this,RunningActivity.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Constant.isServerOn) {
            mLocationHelper.Connect();
        }
    }

    private void initialize() {
        mUIController = new UIController(this);
    }

    private void initializeSyncJobs() {
        MusicTrackMetaData.InitialAccount(this);
        mContentResolver = getContentResolver();
        mContentResolver.setSyncAutomatically(MusicTrackMetaData.mAccount,
                MusicTrackMetaData.AUTHORITY, true);
    }

    private void initializeLocation() {
        mLocationHelper = new LocationHelper(this.getApplicationContext());

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUIController.onActivityRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUIController.onActivityResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUIController.onActivitySaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUIController.onActivityPause();
    }

    @Override
    protected void onStop() {
        if (Constant.isServerOn) {
            mLocationHelper.unregisterPeriodicSyncs();
            mLocationHelper.Disconnect();
        }
        super.onStop();
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        goToRunningPage();
//    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, SyncService.class);
        stopService(intent);
        mLocationHelper.Disconnect();
        mUIController.onActivityDestroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_runner, menu);
        return false;
    }

    @Override
    public void onBackPressed() {
        mUIController.onActivityBackPressed();
    }
}
