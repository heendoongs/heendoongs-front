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
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        println("로그인 체크용 토큰 확인" + getAccessToken())
        return getAccessToken() != null
    }
}