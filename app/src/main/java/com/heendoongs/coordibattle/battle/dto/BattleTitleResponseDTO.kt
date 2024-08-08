package com.heendoongs.coordibattle.battle.dto

/**
 * 배틀 제목 응답 DTO
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

data class BattleTitleResponseDTO(
    val battleId: Long,
    val title: String
)
