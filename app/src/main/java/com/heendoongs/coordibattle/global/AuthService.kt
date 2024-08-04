package com.heendoongs.coordibattle.global

import retrofit2.http.POST

interface AuthService {
    @POST("token/reissue")
    fun refreshToken(): retrofit2.Call<TokenResponse>
}