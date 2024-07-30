package com.heendoongs.coordibattle.battle

import retrofit2.Call
import retrofit2.http.GET
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
 * </pre>
 */

interface BattleService {
    @GET("battle")
    fun getBattleCoordies(
        @Query("memberId") memberId: Long
    ): Call<List<BattleDTO>>
}