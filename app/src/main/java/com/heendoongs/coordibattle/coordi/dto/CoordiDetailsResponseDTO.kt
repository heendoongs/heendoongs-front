package com.heendoongs.coordibattle.coordi.dto

import java.time.LocalDateTime

data class CoordiDetailsResponseDTO(
    val memberId: Long,
    val nickname: String,
    val createDate: LocalDateTime,
    val coordiImage: String,
    val coordiTitle: String,
    val clothesList: List<ClothDetailsResponseDTO>,
    val voteCount: Int,
    val isVotingPeriod: Boolean,
    val isCoordiPeriod: Boolean,
    val isVoted: Boolean
)