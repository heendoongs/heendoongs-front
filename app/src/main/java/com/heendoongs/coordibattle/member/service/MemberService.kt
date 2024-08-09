package com.heendoongs.coordibattle.member.service

import com.heendoongs.coordibattle.coordi.dto.CoordiListResponseDTO
import com.heendoongs.coordibattle.coordi.dto.Page
import com.heendoongs.coordibattle.member.dto.*
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
 * 2024.07.31  	조희정       login  메소드 추가
 * 2024.08.01  	조희정       updateAccount, deleteAccount  메소드 추가
 * 2024.08.04  	조희정       getMyClosetList, getNickname, getMyInfo  메소드 추가
 * </pre>
 */
interface MemberService {
    /**
     * 회원가입
     */
    @POST("signup")
    fun signUp(
        @Body signUpRequestDTO: SignUpRequestDTO
    ): Call<ResponseBody>

    /**
     * 로그인
     */
    @POST("login")
    fun login(
        @Body loginRequestDTO: LoginRequestDTO
    ): Call<ResponseBody>

    /**
     * 내 코디 리스트 조회
     */
    @GET("mycloset/list")
    fun getMyClosetList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<Page<CoordiListResponseDTO>>

    /**
     * 내 닉네임 조회
     */
    @GET("mycloset/nickname")
    fun getNickname(
    ): Call<MyNicknameResponseDTO>

    /**
     * 내 정보 조회
     */
    @GET("myinfo")
    fun getMyInfo(
    ): Call<MyInfoResponseDTO>

    /**
     * 회원 정보 수정
     */
    @PUT("update")
    fun updateAccount(
        @Body memberUpdateRequestDTO : MemberUpdateRequestDTO
    ): Call<ResponseBody>

    /**
     * 회원 탈퇴
     */
    @POST("delete")
    fun deleteAccount(
    ): Call<ResponseBody>

}