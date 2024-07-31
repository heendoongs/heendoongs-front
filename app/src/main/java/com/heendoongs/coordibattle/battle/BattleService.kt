package com.heendoongs.coordibattle.battle

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 배틀 서비스
 * @author 남진수
 * @since 2024.07.30
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	남진수       최초 생성
 * 2024.07.30  	남진수       getBattleCoordies 배틀 페이지 리스트 조회
 * 2024.07.30  	남진수       postBattleResult 배틀 투표결과 저장
 * </pre>
 */

interface BattleService {
    @GET("battle")
    fun getBattleCoordies(
        @Query("memberId") memberId: Long
    ): Call<List<BattleDTO>>

    @POST("battle")
    fun postBattleResult(
        @Body voteRequest: MemberCoordiVoteRequestDTO
    ): Call<BattleResponseDTO>

    @GET("banner")
    fun getCurrentBattles(): Call<List<BannerResponseDTO>>
}