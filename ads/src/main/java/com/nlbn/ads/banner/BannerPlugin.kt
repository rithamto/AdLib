package com.nlbn.ads.banner

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.google.gson.reflect.TypeToken
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.TYPE_ADAPTIVE
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.TYPE_COLLAPSIBLE_BOTTOM
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.TYPE_COLLAPSIBLE_TOP
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.TYPE_STANDARD
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.adUnitId_key
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.cbFetchIntervalSec_key
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.refreshRateSec_key
import com.nlbn.ads.banner.RemoteConfigManager.BannerConfig.Companion.type_key
import com.nlbn.ads.util.CommonFirebase
import org.json.JSONObject


@SuppressLint("ViewConstructor")
class BannerPlugin(
    private val activity: Activity,
    private val adContainer: ViewGroup,
    private val shimmer: ViewGroup,
    private val config: Config
) {
    companion object {

        fun fetchAndActivateRemoteConfig() {
            RemoteConfigManager.fetchAndActivate()
        }

        private var LOG_ENABLED = true

        fun setLogEnabled(enabled: Boolean) {
            LOG_ENABLED = enabled
        }

        internal fun log(message: String) {
            if (LOG_ENABLED) {
                Log.d("BannerPlugin", message)
            }
        }
    }

    class Config {
        lateinit var defaultAdUnitId: String
        lateinit var defaultBannerType: BannerType

        /**
         * Remote config key to retrieve banner config data remotely
         * */
        var configKey: String? = null

        /**
         * Banner refresh rate, in seconds. Pub are recommended to disable auto refresh from dashboard
         * Most of the case this is used to refresh a collapsible banner manually
         * */
        var defaultRefreshRateSec: Int? = null

        /**
         * In seconds, indicate minimum time b/w 2 collapsible banner requests.
         * Only works with BannerType.CollapsibleTop or BannerType.CollapsibleBottom
         * If it is the time to send ad request but the duration to last request collapsible banner < cbFetchInterval,
         * Adaptive banner will be shown instead.
         * */
        var defaultCBFetchIntervalSec: Int = 180

        var loadAdAfterInit = true
    }

    enum class BannerType {
        Standard,
        Adaptive,
        CollapsibleTop,
        CollapsibleBottom
    }

    private var adView: BaseAdView? = null

    init {
        initViewAndConfig()

        if (config.loadAdAfterInit) {
            loadAd()
        }
    }

    private fun initViewAndConfig() {
        var adUnitId = config.defaultAdUnitId
        var bannerType = config.defaultBannerType
        var cbFetchIntervalSec = config.defaultCBFetchIntervalSec
        var refreshRateSec: Int? = config.defaultRefreshRateSec

        if (config.configKey != null) {

            var data = CommonFirebase.getRemoteConfigAds(activity)
            log(
                "data" + data
            )
            data = CommonFirebase.getRemoteConfigStringSingle(config.configKey)
            if (data.isNotEmpty()) {
                CommonFirebase.setRemoteConfigAds(activity, data)
            }else{
                data = CommonFirebase.getRemoteConfigAds(activity)
            }
            addViewBanner(data)


        } else {
            adView = BaseAdView.Factory.getAdView(
                activity,
                adUnitId,
                bannerType,
                refreshRateSec,
                cbFetchIntervalSec, shimmer
            )


            adContainer.addView(
                adView,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )

            log(
                "\n adUnitId = $adUnitId " +
                        "\n bannerType = $bannerType " +
                        "\n refreshRateSec = $refreshRateSec " +
                        "\n cbFetchIntervalSec = $cbFetchIntervalSec"
            )
        }


    }

    fun addViewBanner(data: String) {

        val jsonObject = JSONObject(data)
        if (jsonObject != null) {
            var adUnitId = jsonObject.getString(adUnitId_key)
            var bannerType = when (jsonObject.getString(type_key)) {
                TYPE_STANDARD -> BannerType.Standard
                TYPE_ADAPTIVE -> BannerType.Adaptive
                TYPE_COLLAPSIBLE_TOP -> BannerType.CollapsibleTop
                TYPE_COLLAPSIBLE_BOTTOM -> BannerType.CollapsibleBottom
                else -> BannerType.Adaptive
            }
            var refreshRateSec = jsonObject.getInt(refreshRateSec_key)
            var cbFetchIntervalSec = jsonObject.getInt(cbFetchIntervalSec_key)

            log(
                "\n jsonObject " +
                        "\n adUnitId = $adUnitId " +
                        "\n bannerType = $bannerType " +
                        "\n refreshRateSec = $refreshRateSec " +
                        "\n cbFetchIntervalSec = $cbFetchIntervalSec"
            )

            adView = BaseAdView.Factory.getAdView(
                activity,
                adUnitId,
                bannerType,
                refreshRateSec,
                cbFetchIntervalSec, shimmer
            )


            adContainer.addView(
                adView,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
        } else {
            adContainer.removeAllViews()
        }

    }

    fun loadAd() {
        adView?.loadAd()
    }
}