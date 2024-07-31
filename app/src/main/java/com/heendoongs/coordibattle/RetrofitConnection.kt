package com.heendoongs.coordibattle

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
    private var INSTANCE: Retrofit? = null

    fun getInstance(token: String? = null): Retrofit {
        if (INSTANCE == null || token != null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val clientBuilder = OkHttpClient.Builder().addInterceptor(logging)

            if (token != null) {
                val authInterceptor = AuthInterceptor(token)
                clientBuilder.addInterceptor(authInterceptor)
            }

            val client = clientBuilder.build()

            INSTANCE = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return INSTANCE!!
    }
}