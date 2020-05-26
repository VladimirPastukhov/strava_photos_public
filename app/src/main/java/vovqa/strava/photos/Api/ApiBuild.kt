package vovqa.strava.photos.Api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

fun createApi(token: String = "") = createApi(token, GsonConverterFactory.create())

fun createApi(token: String = "", vararg converterFactory: Converter.Factory): StravaApi =
    Retrofit.Builder()
        .baseUrl("https://www.strava.com/api/v3/")
        .addConverterFactories(converterFactory)
        .client(OkHttpClient.Builder()
            .addInterceptor(addHeaderInterceptor("Authorization","Bearer $token"))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build())
        .build()
        .create(StravaApi::class.java)

fun Retrofit.Builder.addConverterFactories(converterFactories: Array<out Converter.Factory>) = apply {
    converterFactories.forEach { cf ->  this.addConverterFactory(cf) }
}

fun addHeaderInterceptor(k: String, v: String) = Interceptor { chain ->
    val newRequest = chain.request().newBuilder().addHeader(k,v).build()
    chain.proceed(newRequest)
}
