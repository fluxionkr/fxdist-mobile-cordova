package kr.fluxion.fxdist.data

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserData(
    @Expose @SerializedName("user_id") val userId: String,
    @SerializedName("user_pass") val password: String,
    @Expose @SerializedName("user_nm") val name: String,
    @SerializedName("tel_no") val telephoneNumber: String?,
    @SerializedName("hp_no") val cellphoneNumber: String?,
    @SerializedName("email") val email: String?,
    @Expose @SerializedName("userkey") val userKey: String,
    @Expose @SerializedName("user_div") val division: String,
    @Expose @SerializedName("mmcm_id") val mmcmId: String,
    @SerializedName("mmcm_name") val mmcmName: String?,
    @SerializedName("memo") val memo: String?
) : Parcelable