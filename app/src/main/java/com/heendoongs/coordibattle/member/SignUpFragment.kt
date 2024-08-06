package com.heendoongs.coordibattle.member

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.global.RetrofitConnection
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

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        binding.btnSignUp.setOnClickListener {
            signUp()
        }

        messageInit()
        addNicknameTextWatcher()

        return binding.root
    }


    private fun signUp() {
        val loginId = binding.editId.text.toString()
        val password = binding.editPw.text.toString()
        val passwordCheck = binding.editPwChk.text.toString()
        val nickname = binding.editNickname.text.toString()

        // null값 확인
        if (loginId.isEmpty()) {
            showMessage(binding.existId, "아이디를 입력해주세요")
            return
        }

        if (password.isEmpty() || passwordCheck.isEmpty()) {
            showMessage(binding.pwNotMatch, "비밀번호를 입력해주세요")
            return
        }

        if (nickname.isEmpty()) {
            showMessage(binding.existNickname, "닉네임을 입력해주세요")
            return
        }

        // 비밀번호 확인
        if (password != passwordCheck) {
            showMessage(binding.pwNotMatch, "비밀번호가 일치하지 않습니다.")
            return
        }

        val signUpRequest = SignUpRequest(loginId, password, nickname)

        // 회원가입 요청 보내기
        service.signUp(signUpRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("회원가입 성공!")
                    messageInit()
                    (requireActivity() as? MainActivity)?.replaceFragment(LogInFragment(), R.id.fragment_my_closet)
                } else {
                    // 회원가입 실패 처리
                    val errorBody = response.errorBody()?.string()
                    val exceptionDto = Gson().fromJson(errorBody, ExceptionDto::class.java)

                    // 에러 유형에 따라 메시지 표시
                    when (exceptionDto.code) {
                        601 -> showMessage(binding.existId, exceptionDto.message)
                        602 -> showMessage(binding.existNickname, exceptionDto.message)
                        else -> showToast(exceptionDto.message)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    // 에러 메시지 초기화
    private fun messageInit() {
        binding.existId.visibility = View.GONE
        binding.existNickname.visibility = View.GONE
        binding.pwNotMatch.visibility = View.GONE
    }

    // 에러 메시지 보여주기
    private fun showMessage(visibleMessage: TextView, message: String) {
        // 모든 메시지를 GONE으로 설정
        messageInit()

        // 전달된 메시지 설정하고 VISIBLE로 설정
        visibleMessage.text = message
        visibleMessage.visibility = View.VISIBLE
    }

    /**
     * 닉네임 3글자가 넘어가면 경고메시지
     */
    private fun addNicknameTextWatcher() {
        binding.editNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 3) {
                    showMessage(binding.existNickname, "닉네임은 3글자 이하로 입력해주세요.")
                    binding.btnSignUp.isEnabled = false
                } else {
                    binding.existNickname.visibility = View.GONE
                    binding.btnSignUp.isEnabled = true
                }
            }
        })
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}