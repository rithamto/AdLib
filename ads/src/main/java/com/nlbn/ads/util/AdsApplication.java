package com.nlbn.ads.util;

import android.app.Application;
import android.util.Log;

import java.util.List;

public abstract class AdsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppUtil.BUILD_DEBUG = buildDebug();
        Log.i("Application", " run debug: " + AppUtil.BUILD_DEBUG);
        Admob.getInstance().initAdmob(this, getListTestDeviceId());
        if (enableAdsResume()) {
            AppOpenManager.getInstance().init(this, getResumeAdId());
        }
        if (enableAdjustTracking()) {
            Adjust.getInstance().init(this, getAdjustToken());
        }

    }

    public abstract boolean enableAdsResume();

    protected boolean enablePreloadAdsResume() {
        return false;
    }

    protected boolean enableAdjustTracking() {
        return false;
    }

    public abstract List<String> getListTestDeviceId();

    public abstract String getResumeAdId();

    protected String getAdjustToken() {
        return null;
    }


    public abstract Boolean buildDebug();
}
