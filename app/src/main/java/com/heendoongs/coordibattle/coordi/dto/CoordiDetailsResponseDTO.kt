package com.heendoongs.coordibattle.coordi.dto

import java.time.LocalDateTime

/**
 * 코디 상세정보 응 DTO
 * @author 남진수
 * @since 2024.07.31
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.31  	남진수       최초 생성
 * 2024.08.01  	남진수       투표여부, 기간여부 추가
 * </pre>
 */

data class CoordiDetailsResponseDTO(
    val memberId: Long,
    val nickname: String,
    val createDate: LocalDateTime,
    val coordiImage: String,
    val coordiTitle: String,
    val clothesList: List<ClothDetailsResponseDTO>,
    val voteCount: Int,
    val isVotingPeriod: Boolean,
    val isCoordiPeriod: Boolean,
    val isVoted: Boolean
)