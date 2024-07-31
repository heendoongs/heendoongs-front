package com.heendoongs.coordibattle.member

/**
 * 멤버 API 관련 데이터 클래스
 * @author 조희정
 * @since 2024.07.28
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	조희정       최초 생성
 * </pre>
 */
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
