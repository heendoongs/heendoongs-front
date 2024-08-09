package com.heendoongs.coordibattle.global

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "pref"
    }

    fun saveAccessToken(accessToken: String) {
        with(prefs.edit()) {
            putString("access_token", accessToken)
            apply()
        }

        try {
            val memberId = JwtUtils.getMemberIdFromJWT(accessToken)
            if (memberId != null) {
                saveMemberId(memberId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveMemberId(memberId: Long) {
        with(prefs.edit()) {
            putLong("memberId", memberId)
            apply()
        }
    }

    fun saveRefreshToken(refreshToken: String) {
        with(prefs.edit()) {
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString("access_token", null)

    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    fun clearTokens() {
        with(prefs.edit()) {
            remove("access_token")
            remove("refresh_token")
            remove("memberId")
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    // memberId 반환하기
    fun getMemberId(): Long? = prefs.getLong("memberId", -1L)
}