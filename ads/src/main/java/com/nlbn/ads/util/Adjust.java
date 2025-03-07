package com.nlbn.ads.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adjust.sdk.AdjustAdRevenue;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
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
        String environment = AppUtil.BUILD_DEBUG ? AdjustConfig.ENVIRONMENT_SANDBOX : AdjustConfig.ENVIRONMENT_PRODUCTION;
        AdjustConfig config = new AdjustConfig(context, appToken, environment);
        if (AppUtil.BUILD_DEBUG) {
            config.setLogLevel(LogLevel.VERBOSE);
        }
        com.adjust.sdk.Adjust.initSdk(config);
        context.registerActivityLifecycleCallbacks(this);
    }

    public void trackAdRevenue(double revenue, String currency) {
        if (adsApplication == null) return;
        if (!adsApplication.enableAdjustTracking()) return;

        /// Track ad revenue
        AdjustAdRevenue adRevenue = new AdjustAdRevenue("admob_sdk");
        adRevenue.setRevenue(revenue, currency);
        com.adjust.sdk.Adjust.trackAdRevenue(adRevenue);

        /// Track event revenue
        String revenueEventToken = adsApplication.getAdjustRevenueEventToken();
        if (revenueEventToken == null || revenueEventToken.isEmpty()) return;
        AdjustEvent adjustEvent = new AdjustEvent(revenueEventToken);
        adjustEvent.setRevenue(revenue, currency);
        com.adjust.sdk.Adjust.trackEvent(adjustEvent);
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
