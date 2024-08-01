package com.heendoongs.coordibattle.coordi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CoordiService {
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
}