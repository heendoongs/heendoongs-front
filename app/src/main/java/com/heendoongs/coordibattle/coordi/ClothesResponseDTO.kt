package com.heendoongs.coordibattle.coordi

/**
 * 옷 리스트(타입별) DTO
 * @author 임원정
 * @since 2024.08.02
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.02  	임원정       최초 생성
 * </pre>
 */

data class ClothesResponseDTO(
    val clothId: Long,
    val type: String,
    val clothImageURL: String
)