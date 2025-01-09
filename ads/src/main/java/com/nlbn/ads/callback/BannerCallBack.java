package com.nlbn.ads.callback;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;

public class BannerCallBack {
    public void onAdFailedToLoad(LoadAdError i) {}

    public void onAdFailedToShow(AdError adError) {}

    public void onAdLoaded() {}

    public void onAdShow() {}

    public void onAdClicked() {}

    public void onAdImpression() {}

    public void onEarnRevenue(long Revenue, String currency) {}
}