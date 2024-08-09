package com.heendoongs.coordibattle.battle.service

import com.heendoongs.coordibattle.battle.dto.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
 * 2024.07.31   임원정       getCurrentBattles 메소드 추가
 * 2024.08.01   임원정       getBattleTitles 메소드 추가
 * </pre>
 */

interface BattleService {

    /*
     * 배틀 페이지 불러오기
     */
    @GET("battle")
    fun getBattleCoordies(): Call<List<BattleDTO>>

    /*
     * 배틀 결과
     */
    @POST("battle")
    fun postBattleResult(
        @Body voteRequest: MemberCoordiVoteRequestDTO
    ): Call<BattleResponseDTO>

    /**
     * 배너 출력
     */
    @GET("battle/banner")
    fun getCurrentBattles(): Call<List<BannerResponseDTO>>

    /**
     * 배틀 제목 반환
     */
    @GET("battle/title")
    fun getBattleTitles(): Call<List<BattleTitleResponseDTO>>
}