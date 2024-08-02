package com.heendoongs.coordibattle.battle

import java.time.LocalDate

/**
 * 배너 응답 DTO
 * @author 임원정
 * @since 2024.07.31
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.31  	임원정       최초 생성
 * </pre>
 */

data class BannerResponseDTO(
    val battleId: Long,
    val battleTitle: String,
    val bannerImageURL: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodType: Char
)
