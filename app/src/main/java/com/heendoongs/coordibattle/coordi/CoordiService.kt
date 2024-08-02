package com.heendoongs.coordibattle.coordi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 코디 Service
 * @author 임원정
 * @since 2024.08.01
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.01  	임원정       최초 생성
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

    @GET("coordi/details")
    fun getCoordiDetails(
        @Query("memberId") memberId: Long,
        @Query("coordiId") coordiId: Long
    ): Call<CoordiDetailsResponseDTO>

    @GET("coordi/like")
    fun likeCoordi(
        @Query("memberId") memberId: Long,
        @Query("coordiId") coordiId: Long
    ): Call<CoordiDetailsResponseDTO>

    /**
     * 코디 리스트 (필터 - 배틀별, 최신순, 랭킹순)
     */
    @POST("coordi/list/filter")
    fun getCoordiListWithFilter(
        @Body requestDTO: CoordiFilterRequestDTO
    ): Call<Page<CoordiListResponseDTO>>
}