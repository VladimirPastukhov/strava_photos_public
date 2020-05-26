package vovqa.strava.photos.Api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class Activity (
    val id: String,
    val name: String,
    @SerializedName("start_date")
    val startDate: Date,
    @SerializedName("start_date_local")
    val startDateLocal: Date,

    @SerializedName("photos")
    @Expose
    val photos: Photos
)


data class Photos(
    @SerializedName("count")
    @Expose
    val count: String
)

data class Photo(
    val id: String?,
    @SerializedName("unique_id")
    val uniqueId: String,
    val urls: PhotoUrls
)

data class PhotoUrls(
    @SerializedName(value = "0", alternate = arrayOf("100", "200", "600", "1000", "2000"))
    val url: String
)

data class Token(
    @SerializedName("access_token")
    val accessToken: String
)
