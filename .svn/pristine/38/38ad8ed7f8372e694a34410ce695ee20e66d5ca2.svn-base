package kr.co.kit.kitdist.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationData(
    @SerializedName("save") val result: String,
    @SerializedName("gps_off") val turnGPSInfoOff: Boolean = false
) : Parcelable

data class LocationReqBody(
    @SerializedName("user_id") val userId: String,
    @SerializedName("userkey") val userKey: String,
    @SerializedName("user_nm") val userName: String,
    @SerializedName("user_div") val userDiv: String,
    @SerializedName("mmcm_id") val mmcmId: String,
    @SerializedName("latitude") val latitude: String,
    @SerializedName("longitude") val longitude: String,
    @SerializedName("gps_off") val turnGPSInfoOff: Boolean = false
)