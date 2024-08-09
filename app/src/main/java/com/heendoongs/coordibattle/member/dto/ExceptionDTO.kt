package com.heendoongs.coordibattle.member.dto

data class ExceptionDTO(
    val code: Int,
    val message: String,
    val validationErrors: Map<String, String>? = null
)
