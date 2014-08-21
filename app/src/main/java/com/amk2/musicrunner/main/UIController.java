
package com.amk2.musicrunner.main;

import com.amk2.musicrunner.R;
import com.amk2.musicrunner.RunningTabContentFactory;
import com.amk2.musicrunner.discover.DiscoverFragment;
import com.amk2.musicrunner.my.MyFragment;
import com.amk2.musicrunner.my.PastRecordFragment;
import com.amk2.musicrunner.setting.SettingFragment;
import com.amk2.musicrunner.start.StartFragment;
import com.amk2.musicrunner.start.StartFragment.StartTabFragmentListener;
//import com.amk2.musicrunner.start.WeatherFragment;
import com.amk2.musicrunner.weather.WeatherFragment;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Manipulate most of UI controls in this app. Note: Operate the UI as far as
 * possible in this class, not in main activity.
 *
 * @author DannyLin
 */
public class UIController implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {

    private static final String TAG = "UIController";
    private static final int TAB_SIZE = 4;

    private final MusicRunnerActivity mMainActivity;

    private TabHost mTabHost;

    private FragmentManager mFragmentManager;
    private ActionBar mActionBar;
    private SwipeControllableViewPager mViewPager;
    private MainTabViewPagerAdapter mMainPagerAdapter;

    // Fragments for each tab
    private MyFragment mMyFragment;
    private PastRecordFragment mPastRecordFragment;
    private StartFragment mStartFragment;
    private WeatherFragment mWeatherFragment;
    //private DiscoverFragment mDiscoverFragment;
    private SettingFragment mSettingFragment;

    public static class TabState {
        public static final int START = 0;
        public static final int MY = 1;
        public static final int WEATHER = 2;
        public static final int SETTING = 3;
    }

    public static class TabTag {
        public static final String START_TAB_TAG = "start_tab_tag";
        public static final String MY_TAB_TAG = "my_tab_tag";
        public static final String WEATHER_TAB_TAG = "weather_tab_tag";
        public static final String SETTING_TAB_TAG = "setting_tab_tag";
    }

    public static class FragmentTag {
        public static final String START_FRAGMENT_TAG = "start_fragment";
        public static final String WEATHER_FRAGMENT_TAG = "weather_fragment";
        public static final String MY_FRAGMENT_TAG = "my_fragment";
        public static final String PAST_RECORD_FRAGMENT_TAG = "past_record_fragment";
        //public static final String DISCOVER_FRAGMENT_TAG = "discover_fragment";
        public static final String SETTING_FRAGMENT_TAG = "setting_fragment";
    }

    public UIController(MusicRunnerActivity activity) {
        mMainActivity = activity;
        mFragmentManager = activity.getFragmentManager();
        mActionBar = activity.getActionBar();
    }

    public void onActivityCreate(Bundle savedInstanceState) {
        initialize();
    }

    private void initialize() {
        initFragments();
        initViewPager();
        initTabs();
        mViewPager.setCurrentItem(TabState.START);
    }

    private void initTabs() {
        mTabHost = (TabHost)mMainActivity.findViewById(android.R.id.tabhost);
        mTabHost.setup();
        addTab(TabTag.START_TAB_TAG,mMainActivity.getString(R.string.start_tab));
        addTab(TabTag.MY_TAB_TAG,mMainActivity.getString(R.string.my_tab));
        addTab(TabTag.WEATHER_TAB_TAG,mMainActivity.getString(R.string.weather_tab));
        addTab(TabTag.SETTING_TAB_TAG,mMainActivity.getString(R.string.setting_tab));
        mTabHost.setOnTabChangedListener(this);
        setTabClickListener();
    }

    private void setTabClickListener() {
        if (mViewPager != null) {
            // Start
            mTabHost.getTabWidget().getChildTabViewAt(TabState.START).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.START);
                    //if(mMainPagerAdapter.getFragment(TabState.START) instanceof WeatherFragment) {
                        //((WeatherFragment) mMainPagerAdapter.getFragment(TabState.START)).onBackPressed();
                    //}
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });

            // My
            mTabHost.getTabWidget().getChildTabViewAt(TabState.MY).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.MY);
                    if(mMainPagerAdapter.getFragment(TabState.MY) instanceof PastRecordFragment) {
                        ((PastRecordFragment) mMainPagerAdapter.getFragment(TabState.MY)).onBackPressed();
                    }
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });

            // Discover
            mTabHost.getTabWidget().getChildTabViewAt(TabState.WEATHER).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.WEATHER);
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });

            // Setting
            mTabHost.getTabWidget().getChildTabViewAt(TabState.SETTING).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewPager.setCurrentItem(TabState.SETTING);
                    Log.d(TAG, "Set current position = " + mTabHost.getCurrentTab());
                }
            });

        }
    }

    private void addTab(String tag, String labelText) {
        View tabView = getTabView(tag);
        mTabHost.addTab(mTabHost.newTabSpec(tag).setIndicator(tabView)
                .setContent(new RunningTabContentFactory(mMainActivity)));
    }

    private View getTabView(String tag) {
        LayoutInflater layoutInflater = LayoutInflater.from(mMainActivity);
        View tabView = new View(mMainActivity);
        if(TabTag.START_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.start_tab, null);
        } else if(TabTag.MY_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.my_tab, null);
        } else if(TabTag.WEATHER_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.discover_tab, null);
        } else if(TabTag.SETTING_TAB_TAG.equals(tag)) {
            tabView = layoutInflater.inflate(R.layout.setting_tab, null);
        }
        return tabView;
    }

    /**
     * Create all fragments and add as children of the view pager. The pager
     * adapter will only change the visibility(show/hide). It'll never
     * create/destroy fragments. If it's after screen rotation, the fragment
     * have been recreated by the FragmentManager. So first see if there're
     * already the target fragment existing.
     */
    private void initFragments() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        // Init StartFragment
        mStartFragment = (StartFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.START_FRAGMENT_TAG);
        if (mStartFragment == null) {
            mStartFragment = new StartFragment();
            transaction.add(R.id.tab_pager, mStartFragment, FragmentTag.START_FRAGMENT_TAG);
        }

        // Init MyFragment
        mMyFragment = (MyFragment) mFragmentManager.findFragmentByTag(FragmentTag.MY_FRAGMENT_TAG);
        if (mMyFragment == null) {
            mMyFragment = new MyFragment();
            transaction.add(R.id.tab_pager, mMyFragment, FragmentTag.MY_FRAGMENT_TAG);
        }

        // Init SettingFragment
        mSettingFragment = (SettingFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.SETTING_FRAGMENT_TAG);
        if (mSettingFragment == null) {
            mSettingFragment = new SettingFragment();
            transaction.add(R.id.tab_pager, mSettingFragment, FragmentTag.SETTING_FRAGMENT_TAG);
        }

        // Init DiscoverFragment
        mWeatherFragment = (WeatherFragment) mFragmentManager
                .findFragmentByTag(FragmentTag.WEATHER_FRAGMENT_TAG);
        if (mWeatherFragment == null) {
            mWeatherFragment = new WeatherFragment();
            transaction.add(R.id.tab_pager, mWeatherFragment, FragmentTag.WEATHER_FRAGMENT_TAG);
        }

        transaction.hide(mStartFragment);
        transaction.hide(mMyFragment);
        transaction.hide(mSettingFragment);
        transaction.hide(mWeatherFragment);
        transaction.commit();
    }

    private void initViewPager() {
        mViewPager = (SwipeControllableViewPager) mMainActivity.findViewById(R.id.tab_pager);
        mMainPagerAdapter = new MainTabViewPagerAdapter(mFragmentManager,TAB_SIZE);
        mViewPager.setAdapter(mMainPagerAdapter);
        mViewPager.setSwipeable(true);
        mViewPager.setOnPageChangeListener(this);
    }

    public void onActivityRestoreInstanceState(Bundle savedInstanceState) {

    }

    public void onActivityResume() {

    }

    public void onActivitySaveInstanceState(Bundle outState) {

    }

    public void onActivityPause() {

    }

    public void onActivityDestroy() {

    }

    public void onActivityBackPressed() {
        switch(mViewPager.getCurrentItem()) {
            case TabState.START:
                //if(mMainPagerAdapter.getFragment(TabState.START) instanceof WeatherFragment) {
                //    ((WeatherFragment) mMainPagerAdapter.getFragment(TabState.START)).onBackPressed();
                //} else {
                    mMainActivity.setResult(Activity.RESULT_OK);
                    mMainActivity.finish();
                //}
                break;
            case TabState.MY:
                if(mMainPagerAdapter.getFragment(TabState.MY) instanceof PastRecordFragment) {
                    ((PastRecordFragment) mMainPagerAdapter.getFragment(TabState.MY)).onBackPressed();
                } else {
                    mViewPager.setCurrentItem(TabState.START);
                }
                break;
            case TabState.WEATHER:
                mViewPager.setCurrentItem(TabState.START);
                break;
            case TabState.SETTING:
                mViewPager.setCurrentItem(TabState.START);
                break;
        }
    }

    /**
     * Control which fragment needed to be displayed according to the tab
     *
     * @author DannyLin
     */
    public class MainTabViewPagerAdapter extends AbstractTabViewPagerAdapter implements
            MyFragment.MyTabFragmentListener {

        private Fragment mFragmentAtMyTab;
        private Fragment mFragmentAtStartTab;

        public MainTabViewPagerAdapter(FragmentManager fm, int size) {
            super(fm, size);
            setSwitchFragmentListener();
        }

        private void setSwitchFragmentListener() {
            mMyFragment.setMyTabFragmentListener(this);
            //mStartFragment.setStartTabFragmentListener(this);
        }

        @Override
        protected Fragment getFragment(int position) {
            switch (position) {
                case TabState.START:
                    if (mFragmentAtStartTab == null) {
                        mFragmentAtStartTab = mStartFragment;
                    }
                    return mFragmentAtStartTab;
                case TabState.MY:
                    if (mFragmentAtMyTab == null) {
                        mFragmentAtMyTab = mMyFragment;
                    }
                    return mFragmentAtMyTab;
                case TabState.WEATHER:
                    return mWeatherFragment;
                case TabState.SETTING:
                    return mSettingFragment;
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof StartFragment && mFragmentAtStartTab instanceof WeatherFragment) {
                return POSITION_NONE;
            }
            if (object instanceof WeatherFragment && mFragmentAtStartTab instanceof StartFragment) {
                return POSITION_NONE;
            }
            if (object instanceof MyFragment && mFragmentAtMyTab instanceof PastRecordFragment) {
                return POSITION_NONE;
            }
            if (object instanceof PastRecordFragment && mFragmentAtMyTab instanceof MyFragment) {
                return POSITION_NONE;
            }
            return POSITION_UNCHANGED;
        }

        /*@Override
        public void onSwitchBetweenStartAndWeatherFragment() {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            if (mFragmentAtStartTab instanceof StartFragment) {
                addWeatherFragment();
                mFragmentAtStartTab = mWeatherFragment;
                ((WeatherFragment) mFragmentAtStartTab).setStartTabFragmentListener(this);
            } else { // Instance of WeatherFragment
                mCurTransaction.remove(mFragmentAtStartTab);
                mFragmentAtStartTab = mStartFragment;
            }
            notifyDataSetChanged();
        }

        private void addWeatherFragment() {
            mWeatherFragment = (WeatherFragment) mFragmentManager
                    .findFragmentByTag(FragmentTag.WEATHER_FRAGMENT_TAG);
            if (mWeatherFragment == null) {
                mWeatherFragment = new WeatherFragment();
                mCurTransaction.add(R.id.tab_pager, mWeatherFragment,
                        FragmentTag.WEATHER_FRAGMENT_TAG);
            }
        }*/

        @Override
        public void onSwitchBetweenMyAndPastRecordFragment() {
            if (mCurTransaction == null) {
                mCurTransaction = mFragmentManager.beginTransaction();
            }
            if (mFragmentAtMyTab instanceof MyFragment) {
                addPastRecordFragment();
                mFragmentAtMyTab = mPastRecordFragment;
                ((PastRecordFragment) mFragmentAtMyTab).setMyTabFragmentListener(this);
            } else { // Instance of PastRecordFragment
                mCurTransaction.remove(mFragmentAtMyTab);
                mFragmentAtMyTab = mMyFragment;
            }
            notifyDataSetChanged();
        }

        private void addPastRecordFragment() {
            mPastRecordFragment = (PastRecordFragment) mFragmentManager
                    .findFragmentByTag(FragmentTag.PAST_RECORD_FRAGMENT_TAG);
            if (mPastRecordFragment == null) {
                mPastRecordFragment = new PastRecordFragment();
                mCurTransaction.add(R.id.tab_pager, mPastRecordFragment,
                        FragmentTag.PAST_RECORD_FRAGMENT_TAG);
            }
        }
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
    public void onPageSelected(int position) {
        mTabHost.setCurrentTab(position);
    }

    @Override
    public void onTabChanged(String tabId) {

    }

}
