package com.heendoongs.coordibattle.coordi

import java.time.LocalDate

data class CoordiDetailsResponseDTO(
    val memberId: Long,
    val nickname: String,
    val createDate: LocalDate,
    val coordiImage: String,
    val coordiTitle: String,
    val clothesList: List<ClothDetailsResponseDTO>,
    val voteCount: Int
)