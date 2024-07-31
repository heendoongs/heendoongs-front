package com.heendoongs.coordibattle.member

import com.heendoongs.coordibattle.battle.BattleDTO
import com.heendoongs.coordibattle.battle.BattleResponseDTO
import com.heendoongs.coordibattle.battle.MemberCoordiVoteRequestDTO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * 멤버 서비스
 * @author 조희정
 * @since 2024.07.30
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	조희정       최초 생성
 * 2024.07.30  	조희정       signUp 메소드 추가
 * </pre>
 */
interface MemberService {
    @POST("login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<ResponseBody>

    @POST("signup")
    fun signUp(
        @Body signUpRequest: SignUpRequest
    ): Call<ResponseBody>

    @GET("mycloset")
    fun getMyCloset(
        @Query("memberId") memberId: Long
    ): Call<MyClosetResponse>

    @GET("myinfo")
    fun getMyInfo(
        @Query("memberId") memberId: Long
    ): Call<MyInfoResponse>

}