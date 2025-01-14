[![](https://jitpack.io/v/rithamto/AdLib.svg)](https://jitpack.io/#rithamto/AdLib)
<h1>nlbnAdsLibraty</h1>
<h3><li>Adding the library to your project: Add the following in your root build.gradle at the end of repositories:</br></h3>

<pre>
  allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }	    
    }
}
</pre>
<h5>Implement library in your app level build.gradle:</h5>
<pre>
 dependencies {
    implementation 'com.github.chiennq44:AdsLibrary:Tag'
    implementation 'com.google.android.gms:play-services-ads:22.1.0'
    //multidex
    implementation "androidx.multidex:multidex:2.0.1"
  }

  defaultConfig {
    multiDexEnabled true
  }
</pre>
<h3><li>Add app id in Manifest:</br></h3>
<pre>
     < meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="@string/app_id" />
</pre>
<h3><li>Init aplication</br></h3>
<pre> < application
   android:name=".MyApplication"
   .
   .
   .../></pre>
<pre>
    public class MyApplication extends AsdApplication {
     override fun onCreate() {
        super.onCreate()
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
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
        return "resume_id";
    }
}

</pre>
<h2>- BannerAds</h2>
<div class="content">
  <h4>View xml</h4>
<pre>< include
        android:id="@+id/include"
        layout="@layout/layout_banner"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        app:layout_constraintBottom_toBottomOf="parent" /> 
   
 </pre>
<h4>Load in ativity</h4>
<pre>
    Admob.getInstance().loadBanner(this,"bannerID");
</pre>
<h4>Load in fragment</h4>
<pre>
  
   Admob.getInstance().loadBannerFragment( mActivity, "bannerID",  rootView)
  
</pre>
</div>
<h2>IntertitialAds</h2>
<div class="content">
  <h3>- Inter Splash</h3>
  <pre>
      public InterCallback interCallback;
      interCallback = new InterCallback(){
            @Override
            public void onNextAction() {
                super.onNextAction();
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }
      };
      Admob.getInstance().loadSplashInterAds2(this,"interstitial_id",3000,interCallback);

     onresume
     Admob.getInstance().onCheckShowSplashWhenFail(this,interCallback,1000);
    
  </pre>
<h3>- InterstitialAds</h3>
  <h4>Create and load interstitialAds</h4>
<pre>
  private InterstitialAd mInterstitialAd;

   Admob.getInstance().loadInterAds(this, "interstitial_id" new InterCallback() {
            @Override
            public void onInterstitialLoad(InterstitialAd interstitialAd) {
                super.onInterstitialLoad(interstitialAd);
                mInterstitialAd = interstitialAd;
            }
        });
</pre>
<h4>Show interstitialAds</h4>
<pre>
   Admob.getInstance().showInterAds(MainActivity.this, mInterstitialAd, new InterCallback() {
                    @Override
                    public void onNextAction() {
                        startActivity(new Intent(MainActivity.this,MainActivity3.class));
                        // Create and load interstitialAds (when not finish activity ) 
                    }});
</pre>
</div>

<h2>- RewardAds</h2>
<div class="content">
  <h4>Init RewardAds</h4>
<pre>  Admob.getInstance().initRewardAds(this,reward_id);</pre>
<h4>Show RewardAds</h4>
<pre>
  Admob.getInstance().showRewardAds(MainActivity.this,new RewardCallback(){
                    @Override
                    public void onEarnedReward(RewardItem rewardItem) {
                        // code here
                    }

                    @Override
                    public void onAdClosed() {
                        // code here
                    }

                    @Override
                    public void onAdFailedToShow(int codeError) {
                       // code here
                    }
                });
</pre>
</div>

<h2>- NativeAds</h2>
<div class="content">
  <h4>View xml</h4>
<pre>
  
    < FrameLayout
        android:id="@+id/native_ads"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
  
</pre>
<h4>Create and show nativeAds</h4>
<pre>
  
     private FrameLayout native_ads;
     
     native_ads = findViewById(R.id.native_ads);
     
      Admob.getInstance().loadNativeAd(this, "native_id", new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                NativeAdView adView = ( NativeAdView) LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_native, null);
                fr_ads1.removeAllViews();
                fr_ads1.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
            }
             @Override
                public void onAdFailedToLoad() {
                    fr_ads1.removeAllViews();
                }
        });
          
</pre>

</div>

<h4>Hide all ads</h4>
<pre>
 Admob.getInstance().setShowAllAds(true);
 true - show all ads
 false - hide all ads
</pre>
