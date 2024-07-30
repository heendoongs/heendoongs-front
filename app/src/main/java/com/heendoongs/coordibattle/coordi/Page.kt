package com.heendoongs.coordibattle.coordi

data class Page<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)
