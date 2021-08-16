package kr.fluxion.fx.data

import com.google.gson.annotations.SerializedName

data class RespData<T>(
    @SerializedName("mode") val mode: String,
    @SerializedName("code") val code: Int,
    @SerializedName("data") val data: T
)