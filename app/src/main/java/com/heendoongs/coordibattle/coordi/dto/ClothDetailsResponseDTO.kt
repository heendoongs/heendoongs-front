package com.heendoongs.coordibattle.coordi.dto

/**
 * 코디상세정보 DTO
 * @author 남진수
 * @since 2024.07.31
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.31  	남진수       최초 생성
 * </pre>
 */

data class ClothDetailsResponseDTO(
    val clothId: Long,
    val brand: String,
    val productName: String,
    val price: Int,
    val clothImageURL: String,
    val productURL: String
)