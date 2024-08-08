package com.heendoongs.coordibattle.global

import retrofit2.http.POST

/**
 * 인증 관련 서비스
 * @author 조희정
 * @since 2024.08.04
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.04  	조희정       최초 생성
 * </pre>
 */
interface AuthService {

    /**
     * 토큰 재발급
     */
    @POST("token/reissue")
    fun refreshToken(): retrofit2.Call<TokenResponse>
}