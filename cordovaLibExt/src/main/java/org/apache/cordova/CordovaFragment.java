package org.apache.cordova;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.apache.cordova.engine.SystemWebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import kr.fluxion.cordovalibext.R;

public class CordovaFragment extends Fragment {

    private static final String TAG = CordovaFragment.class.getSimpleName();

    private static final String ARGS_KEY_URL = "cordova_url";

    private ViewGroup cordovaWebViewContainer;
    protected CordovaWebView appView;

    protected boolean keepRunning = true;

    protected CordovaPreferences preferences;
    protected String launchUrl;
    protected ArrayList<PluginEntry> pluginEntries;
    protected CordovaInterfaceImpl cordovaInterface;

    public CordovaFragment() {
        // Required empty public constructor
    }

    public static CordovaFragment newInstance() {
        return newInstance(null);
    }

    public static CordovaFragment newInstance(String url) {
        CordovaFragment fragment = new CordovaFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_KEY_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        loadConfig();
        super.onCreate(savedInstanceState);

        cordovaInterface = makeCordovaInterface();
        if (savedInstanceState != null) {
            cordovaInterface.restoreInstanceState(savedInstanceState);
        }

        if (getArguments() != null) {
            String url = getArguments().getString(ARGS_KEY_URL, "");
            if (!TextUtils.isEmpty(url)) {
                launchUrl = url;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cordova_ext, container, false);
        cordovaWebViewContainer = root.findViewById(R.id.cordova_webview_container);
        return root;
    }

    protected WebView getSystemWebView() {
        return (SystemWebView) appView.getEngine().getView();
    }

    protected void init() {
        appView = makeWebView();
        createViews();
        if (!appView.isInitialized()) {
            appView.init(cordovaInterface, pluginEntries, preferences);
        }
        cordovaInterface.onCordovaInit(appView.getPluginManager());

        // Wire the hardware volume controls to control media if desired.
        String volumePref = preferences.getString("DefaultVolumeStream", "");
        if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
            if (getActivity() != null) {
                getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            }
        }
    }

    @SuppressWarnings("deprecation")
    protected void loadConfig() {
        preferences = Config.parser.getPreferences();
        launchUrl = Config.parser.getLaunchUrl();
        pluginEntries = Config.parser.getPluginEntries();
    }

    @SuppressWarnings({"ResourceType"})
    protected void createViews() {
        appView.getView().setId(100);
        appView.getView().setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        cordovaWebViewContainer.removeAllViewsInLayout();
        cordovaWebViewContainer.addView(appView.getView());

        if (preferences.contains("BackgroundColor")) {
            try {
                int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
                // Background of activity:
                appView.getView().setBackgroundColor(backgroundColor);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        appView.getView().requestFocusFromTouch();
    }

    public void loadUrl(String url) {
        if (appView == null) {
            init();
        }

        // If keepRunning
        this.keepRunning = preferences.getBoolean("KeepRunning", true);

        appView.loadUrlIntoView(url, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        LOG.d(TAG, "Paused the fragment.");
        if (this.appView != null) {
            boolean keepRunning = this.keepRunning
                    || this.cordovaInterface.activityResultCallback != null;
            this.appView.handlePause(keepRunning);
        }
    }

    public void onNewIntent(Intent intent) {
        if (this.appView != null)
            this.appView.onNewIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        LOG.d(TAG, "Resumed the fragment.");
        if (this.appView == null) {
            return;
        }
        this.appView.handleResume(this.keepRunning);
    }

    @Override
    public void onStop() {
        super.onStop();
        LOG.d(TAG, "Stopped the fragment.");

        if (this.appView == null) {
            return;
        }
        this.appView.handleStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        LOG.d(TAG, "Started the fragment.");

        if (this.appView == null) {
            return;
        }
        this.appView.handleStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        LOG.d(TAG, "CordovaExtFragment.onDestroy()");
        super.onDestroy();

        if (this.appView != null) {
            appView.handleDestroy();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        cordovaInterface.setActivityResultRequestCode(requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOG.d(TAG, "Incoming Result. Request code = " + requestCode);
        super.onActivityResult(requestCode, resultCode, data);
        cordovaInterface.onActivityResult(requestCode, resultCode, data);
    }

    public Object onMessage(String id, Object data) {
        LOG.e(TAG, "[CordovaMessage] id: " + id + ", data: " + data);
        if ("onReceivedError".equals(id)) {
            JSONObject d = (JSONObject) data;
            try {
                this.onReceivedError(d.getInt("errorCode"), d.getString("description"), d.getString("url"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if ("exit".equals(id)) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
        return null;
    }

    public void onReceivedError(final int errorCode, final String description, final String failingUrl) {
        final String errorUrl = preferences.getString("errorUrl", null);
        LOG.d(TAG, "onReceivedError::errorUrl: " + errorUrl);
        // If errorUrl specified, then load it
        if ((errorUrl != null) && (!failingUrl.equals(errorUrl)) && (appView != null)) {
            appView.showWebPage(errorUrl, false, true, null);
        }
        // If not, then display error dialog
        else {
            final boolean exit = !(errorCode == WebViewClient.ERROR_HOST_LOOKUP);
            if (exit) {
                if (appView != null) {
                    appView.getView().setVisibility(View.GONE);
                }
                displayError("Application Error", description + " (" + failingUrl + ")", "OK", exit);
            }
        }
    }

    public void displayError(final String title, final String message, final String button, final boolean exit) {
        // TODO: SHOW DialogFragment
        LOG.d(TAG, "displayError::message: " + message);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        cordovaInterface.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.appView == null) {
            return;
        }
        PluginManager pm = this.appView.getPluginManager();
        if (pm != null) {
            pm.onConfigurationChanged(newConfig);
        }
    }

    /**
     * Called by the system when the user grants permissions
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        try {
            cordovaInterface.onRequestPermissionResult(requestCode, permissions, grantResults);
        } catch (JSONException e) {
            LOG.d(TAG, "JSONException: Parameters fed into the method are not valid");
            e.printStackTrace();
        }
    }

    /**
     * Construct the default web view object.
     * <p/>
     * Override this to customize the webview that is used.
     */
    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    protected CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(getContext(), preferences);
    }

    protected CordovaInterfaceImpl makeCordovaInterface() {
        return new CordovaInterfaceImpl(getActivity()) {
            @Override
            public Object onMessage(String id, Object data) {
                // Plumb this to CordovaActivity.onMessage for backwards compatibility
                return CordovaFragment.this.onMessage(id, data);
            }
        };
    }
}
