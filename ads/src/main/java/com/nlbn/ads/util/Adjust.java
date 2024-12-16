package com.nlbn.ads.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.BuildConfig;
import com.adjust.sdk.LogLevel;
import com.google.android.gms.ads.AdValue;

public class Adjust implements Application.ActivityLifecycleCallbacks {
    private static Adjust INSTANCE;
    AdsApplication adsApplication;

    public static Adjust getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Adjust();
        }
        return INSTANCE;
    }

    public void init(AdsApplication context, String appToken) {
        this.adsApplication = context;
        String environment = BuildConfig.DEBUG ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(context, appToken, environment);
        if (BuildConfig.DEBUG) {
            config.setLogLevel(LogLevel.VERBOSE);
        }
        com.adjust.sdk.Adjust.initSdk(config);
        context.registerActivityLifecycleCallbacks(this);
    }

    public void trackAdRevenue(AdValue adValue) {
        if (adsApplication != null && adsApplication.enableAdjustTracking()) {
            AdjustAdRevenue revenue = new AdjustAdRevenue(AdjustConfig.ENVIRONMENT_PRODUCTION);
            revenue.setRevenue((double) adValue.getValueMicros() / 1000000, adValue.getCurrencyCode());
            com.adjust.sdk.Adjust.trackAdRevenue(revenue);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        com.adjust.sdk.Adjust.onResume();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        com.adjust.sdk.Adjust.onPause();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
