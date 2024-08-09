package com.heendoongs.coordibattle.global

import com.heendoongs.coordibattle.common.MainApplication
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
 * 수정해야해수정해야해수정해야해
 * </pre>
 */
class AuthInterceptor(private val retrofitConnection: RetrofitConnection) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = MainApplication.prefs.getAccessToken()

        val authenticatedRequest = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", accessToken)
                .build()
        } else {
            originalRequest
        }

        val response = chain.proceed(authenticatedRequest)

        // Access token이 만료되었을 경우
        if (response.code == 700) {
            synchronized(this) {
                // Refresh Token을 사용하여 새로운 Access Token을 가져옴
                val newAccessToken = reissueAccessToken()
                if (newAccessToken != null) {
                    MainApplication.prefs.saveAccessToken(newAccessToken)

                    // 새로운 Access Token을 사용하여 원래의 요청을 재시도
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", newAccessToken)
                        .build()


                    return chain.proceed(newRequest)
                }
            }
        }
        return response
    }

    private fun reissueAccessToken(): String? {
        val refreshToken = MainApplication.prefs.getRefreshToken() ?: return null
        val authService = retrofitConnection.getInstance().create(AuthService::class.java)

        // (동기 요청)
        val response = authService.reissueToken(refreshToken).execute()
        return if (response.isSuccessful) {
            response.headers()["Authorization"]
        } else {
            null
        }
    }
}