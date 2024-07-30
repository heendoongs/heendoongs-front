package com.heendoongs.coordibattle.member

import com.heendoongs.coordibattle.battle.BattleDTO
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface MemberService {
    @POST("login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<ResponseBody>

    @POST("signup")
    fun signUp(
        @Body signUpRequest: SignUpRequest
    ): Call<ResponseBody>
}