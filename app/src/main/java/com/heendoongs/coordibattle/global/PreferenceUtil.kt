package com.heendoongs.coordibattle.global

import android.content.Context
import android.content.SharedPreferences

/**
 * Shared Preference 관련 Utils
 * @author 조희정
 * @since 2024.08.02
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.02  	조희정       최초 생성
 * 2024.08.02  	조희정       isLoggedIn 메서드 생성
 * 2024.08.04  	조희정       MemberId 저장 기능 추가
 * </pre>
 */
class PreferenceUtil(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "pref"
    }

    /**
     * Access Token 저장
     */
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

    /**
     * MemberId 저장
     */
    private fun saveMemberId(memberId: Long) {
        with(prefs.edit()) {
            putLong("memberId", memberId)
            apply()
        }
    }

    /**
     * Refresh Token 저장
     */
    fun saveRefreshToken(refreshToken: String) {
        with(prefs.edit()) {
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    /**
     * Access Token 반환
     */
    fun getAccessToken(): String? = prefs.getString("access_token", null)

    /**
     * Refresh Toekn 반환
     */
    fun getRefreshToken(): String? = prefs.getString("refresh_token", null)

    /**
     * Token 삭제
     */
    fun clearTokens() {
        with(prefs.edit()) {
            remove("access_token")
            remove("refresh_token")
            remove("memberId")
            apply()
        }
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    /**
     * MemberId 반환
     */
    fun getMemberId(): Long? = prefs.getLong("memberId", -1L)
}