package com.heendoongs.coordibattle.coordi

import com.google.gson.annotations.SerializedName

data class RankingOrderCoordiListResponseDTO(
    @SerializedName("coordiId") val coordiId: Long,
    @SerializedName("coordiTitle") val coordiTitle: String,
    @SerializedName("coordiImage") val coordiImage: String,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("voteCount") val voteCount: Long
)