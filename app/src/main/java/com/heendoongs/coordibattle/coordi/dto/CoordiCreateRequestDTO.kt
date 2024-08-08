package com.heendoongs.coordibattle.coordi.dto

data class CoordiCreateRequestDTO(
    val title: String,
    val coordiImage: String, // Base64 encoded string
    val clothIds: List<Long>
)