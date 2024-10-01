package com.nlbn.ads.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nlbn.ads.R;

import java.util.ArrayList;
import java.util.Arrays;

public class CommonFirebase {
    public static void setRemoteConfigAds(Context context , String value){
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putString("ConfigAds", value);
        editor.commit();
    }
    public static String getRemoteConfigAds(Context context){
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pre.getString("ConfigAds", "");
    }

//    public static void initRemoteConfig() {
//        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setMinimumFetchIntervalInSeconds(5)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
//        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
//        mFirebaseRemoteConfig.fetchAndActivate();
//    }

    public static boolean getRemoteConfigBoolean(String adUnitId) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return mFirebaseRemoteConfig.getBoolean(adUnitId);
    }

    public static long getRemoteConfigLong(String adUnitId) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        return mFirebaseRemoteConfig.getLong(adUnitId);
    }

    public static ArrayList<String> getRemoteConfigString(String adUnitId) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String object = mFirebaseRemoteConfig.getString(adUnitId);
        String[] arStr = object.split(",");
        return new ArrayList<>(Arrays.asList(arStr));
    }

    public static String getRemoteConfigStringSingle(String adUnitId) {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String object = mFirebaseRemoteConfig.getString(adUnitId);
        return object;
    }
}
