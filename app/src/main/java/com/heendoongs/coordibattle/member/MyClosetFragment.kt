package com.heendoongs.coordibattle.member

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.RetrofitConnection
import com.heendoongs.coordibattle.databinding.FragmentMyClosetBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 마이페이지_내 옷장 프래그먼트
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * 2024.07.31  	조희정       getMyCloset, logout 메소드 생성
 * </pre>
 */

class MyClosetFragment :Fragment() {

    private lateinit var service: MemberService
    private lateinit var binding: FragmentMyClosetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyClosetBinding.inflate(inflater, container, false)

        // SharedPreferences에서 JWT 토큰과 memberId 가져오기
        val sharedPref = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)
        val memberId = sharedPref.getLong("memberId", -1)

        if (token != null && memberId != null) {
            // Retrofit 인스턴스 생성 시 토큰 포함
            service = RetrofitConnection.getInstance(token).create(MemberService::class.java)

            binding.btnMyInfoPage.setOnClickListener {
                (requireActivity() as? MainActivity)?.replaceFragment(MyInfoFragment())
            }

            binding.btnLogout.setOnClickListener {
                logout()
            }

            getMyCloset(memberId)
        } else {
            showToast("로그인 정보가 없습니다. 다시 로그인 해주세요.")
        }


        return binding.root
    }

    private fun getMyCloset(memberId: Long) {
        // MyCloset 요청 보내기
        service.getMyCloset(memberId).enqueue(object : Callback<MyClosetResponse> {
            override fun onResponse(call: Call<MyClosetResponse>, response: Response<MyClosetResponse>) {
                if (response.isSuccessful) {
                    // 성공적인 응답 처리
                    val myClosetResponse = response.body()
                    if (myClosetResponse != null) {
                        binding.nickname.text = myClosetResponse.nickname
                    } else {
                        showToast("데이터를 가져올 수 없습니다.")
                    }
                } else {
                    // 실패한 응답 처리
                    showToast("데이터 가져오기 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MyClosetResponse>, t: Throwable) {
                // 요청 실패 처리
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    private fun logout() {
        val sharedPref = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("jwt_token")
            remove("memberId")
            apply()
        }
        showToast("로그아웃 되었습니다.")
        (requireActivity() as? MainActivity)?.replaceFragment(LogInFragment())
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}