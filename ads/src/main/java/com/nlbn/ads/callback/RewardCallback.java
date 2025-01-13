package com.nlbn.ads.callback;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;

public class RewardCallback {
    public void onAdClosed() {}
    public void onAdFailedToLoad(LoadAdError i) {}
    public void onAdFailedToShow(AdError adError) {}
    public void onEarnedReward(RewardItem rewardItem){}
    public void onAdLoaded() {}
    public void onAdClicked() {}
    public void onEarnRevenue(long Revenue, String Currency){}
}
