package kr.fluxion.fx.api

import kr.fluxion.fx.BuildConfig
import kr.fluxion.fx.data.*
import okhttp3.Interceptor
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.net.CookieHandler
import java.net.CookieManager

interface ServiceAPI {

    @POST(FxAPI.Auth.LOGIN)
    suspend fun login(
        @Body body: LoginReqBody
    ): Response<RespData<UserData>>

    @POST(FxAPI.Location.SEND_INFO)
    suspend fun sendLocation(
        @Body body: LocationReqBody
    ): Response<RespData<LocationData>>

    companion object {

        fun getApi(): ServiceAPI = create(BuildConfig.BASE_URL)

        @Suppress("SameParameterValue")
        private fun create(url: String, vararg interceptors: Interceptor?): ServiceAPI {
            CookieHandler.getDefault() ?: CookieHandler.setDefault(CookieManager())
            return create(getOkHttpClient(*interceptors), url)
        }

        private fun create(okHttpClient: OkHttpClient, url: String): ServiceAPI = Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceAPI::class.java)

        private fun getOkHttpClient(
            vararg interceptors: Interceptor?
        ): OkHttpClient = OkHttpClient.Builder().apply {
            addInterceptor(getLogger())
            interceptors.forEach { interceptor ->
                interceptor?.let { addInterceptor(it) }
            }
            cookieJar(JavaNetCookieJar(CookieHandler.getDefault()))
        }.build()

        private fun getLogger(): HttpLoggingInterceptor =
            HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT).also { interceptor ->
                interceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            }
    }
}