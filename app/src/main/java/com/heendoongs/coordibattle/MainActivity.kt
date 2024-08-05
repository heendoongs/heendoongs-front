package com.heendoongs.coordibattle

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.heendoongs.coordibattle.battle.BattleFragment
import com.heendoongs.coordibattle.coordi.CoordiFragment
import com.heendoongs.coordibattle.coordi.HomeFragment
import com.heendoongs.coordibattle.databinding.ActivityMainBinding
import com.heendoongs.coordibattle.global.PreferenceUtil
import com.heendoongs.coordibattle.member.LogInFragment
import com.heendoongs.coordibattle.member.MyClosetFragment
import com.heendoongs.coordibattle.view.ProgressDialog

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
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Shared Preference 설정파일
        prefs = PreferenceUtil(applicationContext)

        // 뷰 바인딩
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 하단바 설정
        makeStatusBarTransparent()
        setBottomNavigation()

        // 로딩 다이얼로그 생성
        progressDialog = ProgressDialog(this)
        progressDialog?.setCancelable(false)
        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

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
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
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
                    R.id.fragment_coordi -> CoordiFragment()
                    R.id.fragment_battle -> BattleFragment()
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

    fun showLoading() {
        progressDialog?.show()
    }

    fun hideLoading() {
        progressDialog?.dismiss()
    }

    /**
     * SHared Preference 가져오기
     */
    fun getPreferenceUtil(): PreferenceUtil {
        return prefs
    }
}
