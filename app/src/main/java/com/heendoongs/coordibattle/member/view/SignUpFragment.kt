package com.heendoongs.coordibattle.member.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.common.MainActivity
import com.heendoongs.coordibattle.databinding.FragmentSignUpBinding
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.member.dto.ExceptionDTO
import com.heendoongs.coordibattle.member.dto.SignUpRequestDTO
import com.heendoongs.coordibattle.member.service.MemberService
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
 * 2024.08.01  	조희정       messageInit, showMessage 메소드 추가
 * 2024.08.07  	조희정       addTextChangedListenerToEditTexts 메소드 추가
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

        // 뷰 바인딩
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        // Retrofit 인스턴스
        service = RetrofitConnection.getInstance().create(MemberService::class.java)
        
        // 가입하기 버튼
        binding.btnSignUp.setOnClickListener {
            signUp()
        }
        
        //에러 메시지 초기화
        errorMessageInit()

        // 닉네임 3글자 넘는지 여부 확인
        addNicknameTextWatcher()

        return binding.root
    }

    /**
     * 회원가입
     */
    private fun signUp() {
        val loginId = binding.editId.text.toString()
        val password = binding.editPw.text.toString()
        val passwordCheck = binding.editPwChk.text.toString()
        val nickname = binding.editNickname.text.toString()

        // null값 확인
        if (loginId.isEmpty()) {
            showErrorMessage(binding.idError, "아이디를 입력해주세요")
            return
        }

        if (password.isEmpty() || passwordCheck.isEmpty()) {
            showErrorMessage(binding.pwError, "비밀번호를 입력해주세요")
            return
        }

        if (passwordCheck.isEmpty()) {
            showErrorMessage(binding.pwChkError, "비밀번호 확인을 입력해주세요")
            return
        }

        if (nickname.isEmpty()) {
            showErrorMessage(binding.nicknameError, "닉네임을 입력해주세요")
            return
        }

        // 비밀번호 일치 여부 확인
        if (password != passwordCheck) {
            showErrorMessage(binding.pwError, "비밀번호가 일치하지 않습니다.")
            return
        }

        val signUpRequest = SignUpRequestDTO(loginId, password, nickname)

        // 회원가입 요청 보내기
        service.signUp(signUpRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // 요청 성공
                if (response.isSuccessful) {
                    showToast("회원가입 성공!")
                    errorMessageInit()
                    (requireActivity() as? MainActivity)?.replaceFragment(LogInFragment(), R.id.fragment_my_closet)
                // 요청 실패
                } else {
                    val errorBody = response.errorBody()?.string()
                    val exceptionDto = Gson().fromJson(errorBody, ExceptionDTO::class.java)

                    // 에러 유형에 따라 메시지 표시
                    when (exceptionDto.code) {
                        601 -> showErrorMessage(binding.idError, exceptionDto.message)
                        602 -> showErrorMessage(binding.nicknameError, exceptionDto.message)
                        else -> showToast(exceptionDto.message)
                    }
                }
            }

            // 요청 실패
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    /**
     * 에러 메시지 초기화
     */
    private fun errorMessageInit() {
        binding.idError.visibility = View.GONE
        binding.nicknameError.visibility = View.GONE
        binding.pwError.visibility = View.GONE
    }

    /**
     * 에러 메시지 보여주기
     */
    private fun showErrorMessage(visibleMessage: TextView, message: String) {
        errorMessageInit()

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
                    showErrorMessage(binding.nicknameError, "닉네임은 3글자 이하로 입력해주세요.")
                    binding.btnSignUp.isEnabled = false
                } else {
                    binding.nicknameError.visibility = View.GONE
                    binding.btnSignUp.isEnabled = true
                }
            }
        })
    }

    /**
     * 토스트 메시지 띄우기
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}