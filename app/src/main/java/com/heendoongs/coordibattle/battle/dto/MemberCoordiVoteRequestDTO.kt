package com.heendoongs.coordibattle.battle.dto

/**
 * 배틀 DTO
 * @author 남진수
 * @since 2024.07.30
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	남진수       최초 생성
 * 2024.07.30  	남진수       배틀 투표 요청 DTO
 * </pre>
 */

data class MemberCoordiVoteRequestDTO(
    val winnerCoordiId: Long,
    val loserCoordiId: Long
)
