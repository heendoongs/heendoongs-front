package com.heendoongs.coordibattle.global

import com.heendoongs.coordibattle.MainApplication
import okhttp3.Interceptor
import okhttp3.Response

/**
 * JWT 토큰 헤더 추가를 위한 AuthInterceptor
 * @author 조희정
 * @since 2024.07.31
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.31  	조희정       최초 생성
 * </pre>
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = MainApplication.prefs.getAccessToken()

        return if (accessToken != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}