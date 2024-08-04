package com.heendoongs.coordibattle.coordi

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * 코디 Service
 * @author 임원정
 * @since 2024.07.30
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	임원정       최초 생성
 * 2024.07.31   임원정       getCoordiList API 추가
 * 2024.08.01   임원정       getCoordiListWithFilter API 추가
 * </pre>
 */

interface CoordiService {

    /**
     * 코디 리스트 (기본 - 모든배틀, 랭킹순)
     */
    @GET("coordi/list")
    fun getCoordiList(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<Page<CoordiListResponseDTO>>

    /*
     * 코디 상세정보 조회
     */
    @GET("coordi/details")
    fun getCoordiDetails(
        @Query("coordiId") coordiId: Long
    ): Call<CoordiDetailsResponseDTO>

    /*
     * 코디 상세정보 좋아요
     */
    @GET("coordi/like")
    fun likeCoordi(
        @Query("memberId") memberId: Long,
        @Query("coordiId") coordiId: Long
    ): Call<CoordiDetailsResponseDTO>

    @PATCH("coordi/update")
    fun updateCoordi(
        @Query("memberId") memberId: Long,
        @Query("coordiId") coordiId: Long,
        @Body requestDTO: CoordiUpdateRequestDTO
    ): Call<CoordiDetailsResponseDTO>

    @DELETE("coordi/delete")
    fun deleteCoordi(
        @Query("memberId") memberId: Long,
        @Query("coordiId") coordiId: Long?
    ): Call<ResponseBody>

    /**
     * 코디 리스트 (필터 - 배틀별, 최신순, 랭킹순)
     */
    @POST("coordi/list/filter")
    fun getCoordiListWithFilter(
        @Body requestDTO: CoordiFilterRequestDTO
    ): Call<Page<CoordiListResponseDTO>>

    /**
     * 타입별 옷 리스트
     */
    @GET("coordi/clothes")
    fun getClothesList(
        @Query("type") type: String
    ): Call<List<ClothesResponseDTO>>

    /**
     *
     */
    @POST("coordi")
    fun uploadCoordi(@Body request: CoordiCreateRequestDTO): Call<ResponseBody>
}