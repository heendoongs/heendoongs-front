package com.heendoongs.coordibattle.common

import android.app.Application
import com.heendoongs.coordibattle.global.PreferenceUtil

/**
 * 전역 관리를 위한 Application
 * @author 조희정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.02  	조희정       최초 생성
 * </pre>
 */
class MainApplication : Application() {

    // Shared Preference 관리 유틸리티 클래스 정의
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    // 애플리케이션 실행 동안 존재하는 컨텍스트에 prefs 초기화
    override fun onCreate() {
        super.onCreate()
        prefs = PreferenceUtil(applicationContext)
    }
}