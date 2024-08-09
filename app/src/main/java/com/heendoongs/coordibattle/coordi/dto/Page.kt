package com.heendoongs.coordibattle.coordi.dto

/**
 * 페이징 처리용 페이지 클래스
 * @author 임원정
 * @since 2024.07.31
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.31 	임원정       최초 생성
 * </pre>
 */

data class Page<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,
    val size: Int
)
