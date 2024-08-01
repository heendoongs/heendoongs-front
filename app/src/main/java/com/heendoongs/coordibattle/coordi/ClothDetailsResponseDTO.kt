package com.heendoongs.coordibattle.coordi

data class ClothDetailsResponseDTO(
    val clothId: Long,
    val brand: String,
    val productName: String,
    val price: Int,
    val clothImageURL: String,
    val productURL: String
)