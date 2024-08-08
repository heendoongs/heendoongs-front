package com.heendoongs.coordibattle.global

import android.util.Base64
import org.json.JSONObject
import java.io.UnsupportedEncodingException

/**
 * Jwt 관련 Utils
 * @author 조희정
 * @since 2024.08.04
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.04  	조희정       최초 생성
 * </pre>
 */
object JwtUtils {

    /**
     * JWT에서 memberId 추출
     */
    @Throws(Exception::class)
    fun getMemberIdFromJWT(JWTEncoded: String): Long? {
        return try {
            // JWT 디코딩
            val split = JWTEncoded.split(".")
            val body = getJson(split[1])

            // memberId 추출
            val jsonObject = JSONObject(body)
            jsonObject.getLong("memberId")
        } catch (e: UnsupportedEncodingException) {
            throw Exception("Error while decoding JWT", e)
        }
    }

    /**
     * 문자열을 디코딩해 JSON으로 변환
     */
    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        // Base64 URL Safe로 디코딩
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        // 디코딩된 바이트 배열을 UTF-8 문자열로 변환
        return String(decodedBytes, Charsets.UTF_8)
    }
}
