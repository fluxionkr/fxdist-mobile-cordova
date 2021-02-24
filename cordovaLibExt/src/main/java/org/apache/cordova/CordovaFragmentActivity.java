package org.apache.cordova;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import kr.co.aiblab.cordovalibext.R;

public class CordovaFragmentActivity extends AppCompatActivity {

    public static String TAG = CordovaFragmentActivity.class.getSimpleName();

    // Flag to keep immersive mode if set to fullscreen
    protected boolean immersiveMode;

    // Read from config.xml:
    protected CordovaPreferences preferences;

    private ViewGroup mFragmentContainer;

    protected int getFragmentContainerId() {
        return mFragmentContainer.getId();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // need to activate preferences before super.onCreate to avoid "requestFeature() must be called before adding content" exception
        loadConfig();

        String logLevel = preferences.getString("loglevel", "ERROR");
        LOG.setLogLevel(logLevel);

        LOG.i(TAG, "Apache Cordova native platform version " + CordovaWebView.CORDOVA_VERSION + " is starting");
        LOG.d(TAG, "CordovaActivity.onCreate()");

        if (!preferences.getBoolean("ShowTitle", false)) {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        if (preferences.getBoolean("SetFullscreen", false)) {
            LOG.d(TAG, "The SetFullscreen configuration is deprecated in favor of Fullscreen, and will be removed in a future version.");
            preferences.set("Fullscreen", true);
        }
        if (preferences.getBoolean("Fullscreen", false)) {
            // NOTE: use the FullscreenNotImmersive configuration key to set the activity in a REAL full screen
            // (as was the case in previous cordova versions)
            if (!preferences.getBoolean("FullscreenNotImmersive", false)) {
                immersiveMode = true;
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mFragmentContainer = findViewById(R.id.fragment_container);
    }

    @SuppressWarnings("deprecation")
    protected void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(this);
        preferences = parser.getPreferences();
        preferences.setPreferencesBundle(getIntent().getExtras());
        Config.parser = parser;
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     */
    @Override
    protected void onPause() {
        super.onPause();
        LOG.d(TAG, "Paused the activity.");
    }

    /**
     * Called when the activity receives a new intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    /**
     * /**
     * Called when the activity will start interacting with the user.
     */
    @Override
    protected void onResume() {
        super.onResume();
        LOG.d(TAG, "Resumed the activity.");
        // Force window to have focus, so application always
        // receive user input. Workaround for some devices (Samsung Galaxy Note 3 at least)
        this.getWindow().getDecorView().requestFocus();
    }

    /**
     * Called when the activity is no longer visible to the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
        LOG.d(TAG, "Stopped the activity.");
    }

    /**
     * Called when the activity is becoming visible to the user.
     */
    @Override
    protected void onStart() {
        super.onStart();
        LOG.d(TAG, "Started the activity.");
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    @Override
    public void onDestroy() {
        LOG.d(TAG, "CordovaActivity.onDestroy()");
        super.onDestroy();
    }

    /**
     * Called when view focus is changed
     */
    @SuppressLint("InlinedApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && immersiveMode) {
            final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        CordovaFragment cordovaFragment = getCurrentCordovaFragment();
        if (cordovaFragment != null) {
            cordovaFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        CordovaFragment cordovaFragment = getCurrentCordovaFragment();
        if (cordovaFragment != null) {
            cordovaFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 모든 관련 fragment 를 찾아낸 뒤 cordova webview 에
     *
     * @param tag
     * @return
     * @author SEOJAEHWA
     */
    @Nullable
    private CordovaFragment findCordovaFragment(final String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment instanceof CordovaFragment) {
            return (CordovaFragment) fragment;
        }
        return null;
    }

    /**
     * @param fragmentContainerViewId
     * @return
     * @author SEOJAEHWA
     */
    @Nullable
    private CordovaFragment getCurrentCordovaFragment(@IdRes int fragmentContainerViewId) {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(fragmentContainerViewId);
            if (fragment instanceof CordovaFragment) {
                return (CordovaFragment) fragment;
            }
        }
        return null;
    }

    @Nullable
    public CordovaFragment getCurrentCordovaFragment() {
        return getCurrentCordovaFragment(mFragmentContainer.getId());
    }
}
