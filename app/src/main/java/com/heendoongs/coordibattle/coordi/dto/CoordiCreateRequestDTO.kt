package com.heendoongs.coordibattle.coordi.dto

/**
 * 코디 업로드 요청 DTO
 * @author 임원정
 * @since 2024.08.02
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.03  	임원정       최초 생성
 * 2024.08.04   임원정       memberId 필드 제외
 * </pre>
 */

data class CoordiCreateRequestDTO(
    val title: String,
    val coordiImage: String, // Base64 인코딩 된 string
    val clothIds: List<Long>
)