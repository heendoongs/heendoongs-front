package com.heendoongs.coordibattle.battle

import java.time.LocalDate

data class BannerResponseDTO(
    val battleId: Long,
    val battleTitle: String,
    val bannerImageURL: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodType: Char
)
