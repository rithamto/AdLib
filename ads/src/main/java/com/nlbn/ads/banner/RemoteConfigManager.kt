package com.nlbn.ads.banner


import android.app.Activity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.nlbn.ads.banner.BannerPlugin.Companion.log
import com.nlbn.ads.util.CommonFirebase

internal object RemoteConfigManager {

    private val gson by lazy { Gson() }

    fun fetchAndActivate() {
        FirebaseRemoteConfig.getInstance().fetchAndActivate()
    }

    fun getBannerConfig(activity : Activity,key: String): BannerConfig? {
        return getConfig<BannerConfig>(activity,key)
    }

    private inline fun <reified T> getConfig(activity : Activity,configName: String): T? {
        return try {
            log(
                "getConfig"
            )
            var data = CommonFirebase.getRemoteConfigStringSingle(configName)
            gson.fromJson<T>(data, object : TypeToken<T>() {}.type)
        } catch (ignored: Throwable) {
            null
        }
    }

    data class BannerConfig(
        @SerializedName("ad_unit_id")
        val adUnitId: String?,
        @SerializedName("type")
        val type: String?,
        @SerializedName("refresh_rate_sec")
        val refreshRateSec: Int?,
        @SerializedName("cb_fetch_interval_sec")
        val cbFetchIntervalSec: Int?
    ) {
        companion object {
            const val TYPE_STANDARD = "standard"
            const val TYPE_ADAPTIVE = "adaptive"
            const val TYPE_COLLAPSIBLE_TOP = "collapsible_top"
            const val TYPE_COLLAPSIBLE_BOTTOM = "collapsible_bottom"
            const val adUnitId_key = "ad_unit_id"
            const val type_key = "type"
            const val refreshRateSec_key = "refresh_rate_sec"
            const val cbFetchIntervalSec_key = "cb_fetch_interval_sec"
        }
    }
}