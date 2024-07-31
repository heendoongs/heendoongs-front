package com.heendoongs.coordibattle.battle

import java.time.LocalDate

data class BannerResponseDTO(
    val bannerId: Long,
    val bannerTitle: String,
    val bannerImageURL: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val periodType: Char
)
