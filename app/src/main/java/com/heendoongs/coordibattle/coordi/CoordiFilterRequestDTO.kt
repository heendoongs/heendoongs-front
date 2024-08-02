package com.heendoongs.coordibattle.coordi

data class CoordiFilterRequestDTO(
    val battleId: Long? = null,
    val order: String? = "RANKING",
    val page: Int = 0,
    val size: Int = 6
)
