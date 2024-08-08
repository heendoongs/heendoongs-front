package com.heendoongs.coordibattle.global

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.member.view.LogInFragment


/**
 * 프레그먼트 이동 메서드 모음 MoveFragment
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

/**
 * 로그인 프레그먼트로 이동
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
 * 로그인 여부에 따라 프레그먼트 이동
 * @return
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