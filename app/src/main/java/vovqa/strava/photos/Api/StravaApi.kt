package vovqa.strava.photos.Api


import retrofit2.http.*

interface StravaApi {
    @POST("oauth/token")
    @FormUrlEncoded
    suspend fun getTocken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): Token

    @GET("athlete")
    suspend fun getAthleteJson(): String

    @GET("activities")
    suspend fun getActivitiesJson(
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): String

    @GET("activities")
    suspend fun getActivities(
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): List<Activity>

    @GET("activities/{activity_id}")
    suspend fun getActivityJson(@Path("activity_id") activityId: String): String

    @GET("activities/{activity_id}")
    suspend fun getActivity(@Path("activity_id") activityId: Long): Activity

    @GET("activities/{activity_id}/photos")
    suspend fun getActivityPhotosJson(@Path("activity_id") activityId: String): String

    @GET("activities/{activity_id}/photos?size=2000")
    suspend fun getActivityPhotosBig(@Path("activity_id") activityId: String): List<Photo>

    @GET("activities/{activity_id}/photos?size=200")
    suspend fun getActivityPhotosSmall(@Path("activity_id") activityId: String): List<Photo>
}


