package com.heendoongs.coordibattle.member

import com.heendoongs.coordibattle.coordi.CoordiListResponseDTO

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
    val token: String,
    val memberId: Long
)

// 회원가입 요청
data class SignUpRequest(
    val loginId: String,
    val password: String,
    val nickname: String
)

// 내 닉네임 응답
data class MyNicknameResponse(
    val nickname: String
)

// 내 정보 응답
data class MyInfoResponse(
    val loginId: String,
    val nickname: String
)

// 회원 업데이트
data class MemberUpdateRequest(
    val password: String,
    val nickname: String
)

// 에러 코드
data class ExceptionDto(
    val code: Int,
    val message: String,
    val validationErrors: Map<String, String>? = null
)
