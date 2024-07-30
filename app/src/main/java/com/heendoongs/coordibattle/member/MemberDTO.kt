package com.heendoongs.coordibattle.member

// 로그인 요청
data class LoginRequest(
    val loginId: String,
    val password: String
)

// 로그인 응답
data class LoginResponse(
    val token: String, // 예를 들어, 로그인 성공 시 받는 토큰
    val userId: Long
)

// 회원가입 요청
data class SignUpRequest(
    val loginId: String,
    val password: String,
    val nickname: String
)
