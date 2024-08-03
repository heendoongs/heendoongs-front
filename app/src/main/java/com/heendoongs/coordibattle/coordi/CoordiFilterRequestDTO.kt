package com.heendoongs.coordibattle.coordi

/**
 * 코디 리스트 (필터 적용) DTO
 * @author 임원정
 * @since 2024.08.01
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.01  	임원정       최초 생성
 * </pre>
 */

data class CoordiFilterRequestDTO(
    val battleId: Long? = null,
    val order: String? = "RANKING",
    val page: Int = 0,
    val size: Int = 6
)
