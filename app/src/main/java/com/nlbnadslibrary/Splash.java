package com.nlbnadslibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.nlbn.ads.billing.AppPurchase;
import com.nlbn.ads.callback.BillingListener;
import com.nlbn.ads.callback.InterCallback;
import com.nlbn.ads.util.Admob;
import com.google.android.gms.ads.LoadAdError;

import java.util.ArrayList;
import java.util.List;

public class Splash extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    public static String PRODUCT_ID_MONTH = "android.test.purchased";
    public InterCallback interCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        String android_id = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        //Admob.getInstance().setOpenShowAllAds(true);
        //Admob.getInstance().setDisableAdResumeWhenClickAds(true);
        //Admob.getInstance().setOpenEventLoadTimeLoadAdsSplash(true);
       // Admob.getInstance().setOpenEventLoadTimeShowAdsInter(true);
       // Admob.getInstance().setOpenActivityAfterShowInterAds(false);

        interCallback = new InterCallback(){
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }
        };
        // Admob
        AppPurchase.getInstance().setBillingListener(new BillingListener() {
            @Override
            public void onInitBillingListener(int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> listID = new ArrayList<>();
                        listID.add("ca-app-pub-3940256099942544/1033173712");
                        listID.add("ca-app-pub-3940256099942544/1033173712");
                        Admob.getInstance().loadSplashInterAdsFloor(Splash.this,listID,2000, interCallback);
                    }
                });
            }
        }, 5000);

        initBilling();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Admob.getInstance().dismissLoadingDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Admob.getInstance().dismissLoadingDialog();
    }

    private void initBilling() {
        List<String> listINAPId = new ArrayList<>();
        listINAPId.add(PRODUCT_ID_MONTH);
        List<String> listSubsId = new ArrayList<>();
        AppPurchase.getInstance().initBilling(getApplication(),listINAPId,listSubsId);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Admob.getInstance().onCheckShowSplashWhenFail(this,interCallback,1000);
    }
}