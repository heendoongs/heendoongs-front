package com.heendoongs.coordibattle.common

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.WindowCompat
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.battle.view.BattleEntranceFragment
import com.heendoongs.coordibattle.coordi.view.CoordiEntranceFragment
import com.heendoongs.coordibattle.coordi.view.HomeFragment
import com.heendoongs.coordibattle.databinding.ActivityMainBinding
import com.heendoongs.coordibattle.global.PreferenceUtil
import com.heendoongs.coordibattle.member.view.MyClosetFragment


/**
 * 메인 액티비티
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * 2024.07.30  	조희정       하단바 fragment_my_closet 연결 프레그먼트 변경
 * 2024.08.04   남진수       구글 애널리틱스 관련 설정 추가
 * </pre>
 */
class MainActivity : AppCompatActivity() {
    companion object{
        lateinit var prefs : PreferenceUtil
    }

    private lateinit var binding : ActivityMainBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var isReplacingFragment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Shared Preference 설정파일
        prefs = PreferenceUtil(applicationContext)

        // 뷰 바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeStatusBarTransparent()
        setBottomNavigation()

        /**
         * 구글 애널리틱스 관련 설정
         */
        firebaseAnalytics = Firebase.analytics

        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
        FirebaseAnalytics.getInstance(this).setSessionTimeoutDuration(1000000)
        FirebaseAnalytics.getInstance(this).logEvent("debug_logging_enabled", null)
    }

    /**
     * 상태바, 하단바까지 화면 확장
     */
    private fun makeStatusBarTransparent() {
        /*window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }*/
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.WHITE
        }
    }

    /**
     * 하단 내비게이션바 설정
     */
    private fun setBottomNavigation() {
        binding.bottomNavigationView.selectedItemId = R.id.fragment_home
        replaceFragment(HomeFragment(), R.id.fragment_home)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (!isReplacingFragment) {
                val fragment : Fragment? = when (item.itemId) {
                    R.id.fragment_home -> HomeFragment()
                    R.id.fragment_coordi -> CoordiEntranceFragment()
                    R.id.fragment_battle -> BattleEntranceFragment()
                    R.id.fragment_my_closet -> MyClosetFragment()
                    else -> null
                }
                replaceFragment(fragment, item.itemId)
            }
            true
        }
    }

    /**
     * 프래그먼트 교체
     */
    fun replaceFragment(fragment : Fragment?, itemId: Int) {
        if(fragment != null) {
            isReplacingFragment = true
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit()
            binding.bottomNavigationView.selectedItemId = itemId
            isReplacingFragment = false
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

        if(currentFocus is EditText) {
            currentFocus!!.clearFocus()
        }

        return super.dispatchTouchEvent(ev)
    }

    /**
     * SHared Preference 가져오기
     */
    fun getPreferenceUtil(): PreferenceUtil {
        return prefs
    }
}
