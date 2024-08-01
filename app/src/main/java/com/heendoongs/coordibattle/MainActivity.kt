package com.heendoongs.coordibattle

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.battle.BattleFragment
import com.heendoongs.coordibattle.coordi.CoordiFragment
import com.heendoongs.coordibattle.coordi.DetailFragment
import com.heendoongs.coordibattle.coordi.HomeFragment
import com.heendoongs.coordibattle.databinding.ActivityMainBinding
import com.heendoongs.coordibattle.member.LogInFragment
import com.heendoongs.coordibattle.member.MyClosetFragment

/**
 * 메인 액티비티
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * </pre>
 */

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeStatusBarTransparent()
        setBottomNavigation()
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
        replaceFragment(HomeFragment())

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val fragment : Fragment? = when (item.itemId){
                R.id.fragment_home -> HomeFragment()
                R.id.fragment_coordi -> CoordiFragment()
                R.id.fragment_battle -> BattleFragment()
                R.id.fragment_my_closet -> { LogInFragment()
//                    if (isLoggedIn()) {
//                        MyClosetFragment()
//                    } else {
//                        LogInFragment()
//                    }
                }
                else -> null
            }
            replaceFragment(fragment)
            true
        }
    }

    private fun isLoggedIn(): Boolean {
        // sharedPreferences
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    /**
     * 프래그먼트 교체
     */
    fun replaceFragment(fragment : Fragment?) {
        if(fragment!=null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit()
        }
    }

}