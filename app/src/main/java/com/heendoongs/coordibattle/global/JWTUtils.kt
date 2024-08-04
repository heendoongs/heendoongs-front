package com.heendoongs.coordibattle.global

import android.util.Base64
import org.json.JSONObject
import java.io.UnsupportedEncodingException

object JwtUtils {

    // JWT 문자열을 받아서 memberId를 추출하는 메서드
    @Throws(Exception::class)
    fun getMemberIdFromJWT(JWTEncoded: String): Long? {
        return try {
            // JWT 문자열을 '.' 기준으로 분리하여 헤더, 페이로드, 서명을 구분
            val split = JWTEncoded.split(".")
            // 페이로드 부분을 디코딩하여 JSON 문자열로 변환
            val body = getJson(split[1])
            // JSON 객체로 변환하여 "memberId" 필드의 값을 추출
            val jsonObject = JSONObject(body)
            jsonObject.getLong("memberId")
        } catch (e: UnsupportedEncodingException) {
            // 디코딩 중 에러 발생 시 예외 처리
            throw Exception("Error while decoding JWT", e)
        }
    }

    // Base64 URL Safe로 인코딩된 문자열을 디코딩하여 JSON 문자열로 변환하는 메서드
    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        // Base64 URL Safe로 디코딩
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        // 디코딩된 바이트 배열을 UTF-8 문자열로 변환
        return String(decodedBytes, Charsets.UTF_8)
    }
}
