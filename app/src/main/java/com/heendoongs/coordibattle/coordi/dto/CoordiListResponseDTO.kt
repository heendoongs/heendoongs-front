package com.heendoongs.coordibattle.coordi.dto

/**
 * 코디 리스트(기본 - 모든 배틀, 랭킹순) DTO
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * </pre>
 */

data class CoordiListResponseDTO(
    val coordiId: Long,
    val coordiTitle: String,
    val coordiImage: String,
    val nickname: String,
    val voteCount: Long
)