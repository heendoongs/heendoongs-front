package com.heendoongs.coordibattle.global

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit 연결을 위한 RetrofitConnection
 * @author 조희정
 * @since 2024.07.30
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	조희정       최초 생성
 * 2024.07.31  	조희정       JWT 토큰 받아오기 추가
 * </pre>
 */
object RetrofitConnection {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private var retrofit: Retrofit? = null


    fun getInstance(): Retrofit {
        // 로그인이 필요한
        if (retrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(this))
                .addInterceptor(loggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        }
        return retrofit!!
    }
}