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
        @Query("coordiId") coordiId: Long
    ): Call<CoordiDetailsResponseDTO>
}