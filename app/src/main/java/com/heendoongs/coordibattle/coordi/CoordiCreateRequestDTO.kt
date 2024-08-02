package com.heendoongs.coordibattle.coordi

data class CoordiCreateRequestDTO(
    val memberId: Long,
    val title: String,
    val coordiImage: String, // Base64 encoded string
    val clothIds: List<Long>
)