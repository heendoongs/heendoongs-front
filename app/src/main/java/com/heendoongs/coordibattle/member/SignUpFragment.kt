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
import com.heendoongs.coordibattle.databinding.FragmentLogInBinding
import com.heendoongs.coordibattle.databinding.FragmentSignUpBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 회원가입 프래그먼트
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * 2024.07.30  	조희정       signUp 메소드 추가
 * </pre>
 */

class SignUpFragment : Fragment() {

    private lateinit var service: MemberService
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        service = RetrofitConnection.getInstance().create(MemberService::class.java)

        binding.btnSignUp.setOnClickListener {
            signUp()
        }

        return binding.root
    }


    private fun signUp() {
        val loginId = binding.editId.text.toString()
        val password = binding.editPw.text.toString()
        val nickname = binding.editNickname.text.toString()

        val signUpRequest = SignUpRequest(loginId, password, nickname)

        // 회원가입 요청 보내기
        service.signUp(signUpRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    println("회원가입 성공!")
                    showToast("회원가입 성공!")
                } else {
                    showToast("회원가입 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}