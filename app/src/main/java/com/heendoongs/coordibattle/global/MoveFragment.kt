package com.heendoongs.coordibattle.global

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.member.view.LogInFragment


/**
 * 현재 프래그먼트에서 로그인 프래그먼트로 네비게이션하는 함수입니다.
 */
fun Fragment.navigateToLoginFragment() {
    val parentViewGroup = view?.parent as? ViewGroup
    if (parentViewGroup != null) {
        val loginFragment = LogInFragment()
        parentFragmentManager.beginTransaction()
            .replace(parentViewGroup.id, loginFragment)
            .addToBackStack(null)
            .commit()
    }

}

/**
 * 현재 프래그먼트에서 로그인 상태를 확인하고, 로그인이 되어 있지 않으면 로그인 프래그먼트로 이동합니다.
 * @return 로그인이 되어 있으면 true, 아니면 false를 반환합니다.
 */
fun Fragment.checkLoginAndNavigate(): Boolean {
    val preferenceUtil = PreferenceUtil(requireContext())
    return if (!preferenceUtil.isLoggedIn()) {
        navigateToLoginFragment()
        false
    } else {
        true
    }
}