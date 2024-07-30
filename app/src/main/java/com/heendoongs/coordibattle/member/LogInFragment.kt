package com.heendoongs.coordibattle.member

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.RetrofitConnection
import com.heendoongs.coordibattle.battle.BattleService
import com.heendoongs.coordibattle.databinding.FragmentLogInBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Member

/**
 * 로그인 프래그먼트
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

class LogInFragment : Fragment() {

    private lateinit var service: MemberService
    private lateinit var binding: FragmentLogInBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLogInBinding.inflate(inflater, container, false)

        service = RetrofitConnection.getInstance().create(MemberService::class.java)

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.btnSignUpPage.setOnClickListener {
            (requireActivity() as? MainActivity)?.replaceFragment(SignUpFragment())
        }

        return binding.root
    }


    private fun login() {
        val loginId = binding.editId.text.toString()
        val password = binding.editPw.text.toString()

        val loginRequest = LoginRequest(loginId, password)

        // 로그인 요청 보내기
        service.login(loginRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 성공적인 응답 처리
                    val responseBody = response.body()?.string() // 응답 본문 읽기
                    if (responseBody != null && responseBody.isNotEmpty()) {
                        showToast("로그인 성공! 환영합니다.")
                    } else {
                        showToast("로그인 성공하지만 서버에서 응답 메시지가 없습니다.")
                    }
                } else {
                    // 실패한 응답 처리
                    showToast("로그인 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 요청 실패 처리
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}