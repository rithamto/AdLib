package com.nlbnadslibrary;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nlbn.ads.billing.AppPurchase;

import com.nlbn.ads.callback.AdCallback;
import com.nlbn.ads.util.AppOpenManager;
import com.nlbn.ads.util.AdsApplication;
import com.nlbn.ads.util.AppUtil;
import com.nlbn.ads.util.AppsFlyer;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends AdsApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        AppOpenManager.getInstance().disableAppResumeWithActivity(Splash.class);
        AppOpenManager.getInstance().setResumeCallback(new AdCallback(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                Log.e("TAG", "AppOpenManager: "+"onAdClosed" );
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.e("TAG", "AppOpenManager: "+"onAdLoaded" );
            }
        });
        initRemoteConfig();
        AppsFlyer.getInstance().initAppFlyer(this, "", true);

    }

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return getString(R.string.admod_app_open_ad_id);
    }

    @Override
    public Boolean buildDebug() {
        return true;
    }



    public static void initRemoteConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate();
    }


}
