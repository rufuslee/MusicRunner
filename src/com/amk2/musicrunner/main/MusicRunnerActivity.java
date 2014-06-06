package com.amk2.musicrunner.main;

import com.amk2.musicrunner.Constant;
import com.amk2.musicrunner.MusicTrackMetaData;
import com.amk2.musicrunner.R;
import com.amk2.musicrunner.start.LocationHelper;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.view.Menu;

import java.util.concurrent.ExecutionException;

/**
 * Main activity of MusicRunner+
 *
 * @author DannyLin
 */
public class MusicRunnerActivity extends Activity{

	private UIController mUIController;
    private ContentResolver mContentResolver;
    private LocationHelper mLocationHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_runner);
		initialize();
        initializeLocation();
        initializeSyncJobs();

		mUIController.onActivityCreate(savedInstanceState);
        mLocationHelper.Connect();
	}

    @Override
    protected void onStart() {
        super.onStart();

    }

	private void initialize() {
		mUIController = new UIController(this);
	}

    private void initializeSyncJobs() {
        MusicTrackMetaData.InitialAccount(this);
        mContentResolver = getContentResolver();
        mContentResolver.setSyncAutomatically(MusicTrackMetaData.mAccount, MusicTrackMetaData.AUTHORITY, true);
/*
        Bundle bundle1 = new Bundle();
        bundle1.putString(Constant.SYNC_UPDATE, Constant.UPDATE_WEATHER);
        ContentResolver.addPeriodicSync(MusicTrackMetaData.mAccount, MusicTrackMetaData.AUTHORITY, bundle1, Constant.ONE_MINUTE);
        Bundle bundle2 = new Bundle();
        bundle2.putString(Constant.SYNC_UPDATE, Constant.UPDATE_UBIKE);
        ContentResolver.addPeriodicSync(MusicTrackMetaData.mAccount, MusicTrackMetaData.AUTHORITY, bundle2, Constant.ONE_MINUTE);
*/    }

    private void initializeLocation() {
        mLocationHelper = new LocationHelper(this);
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
        super.onStop();
    }
	@Override
	protected void onDestroy() {
		super.onDestroy();
        mLocationHelper.Disconnect();
		mUIController.onActivityDestroy();
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
