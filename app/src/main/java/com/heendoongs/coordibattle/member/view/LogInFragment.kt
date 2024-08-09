package com.heendoongs.coordibattle.member.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.common.MainActivity
import com.heendoongs.coordibattle.common.MainApplication
import com.heendoongs.coordibattle.coordi.view.HomeFragment
import com.heendoongs.coordibattle.databinding.FragmentLogInBinding
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.member.dto.LoginRequestDTO
import com.heendoongs.coordibattle.member.service.MemberService
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
 * 2024.07.30  	조희정       login 메소드 추가
 * 2024.08.02  	조희정       login 메소드에 access/refresh 토큰 저장 기능 추가
 * 2024.08.07  	조희정       addTextChangedListenerToEditTexts 메소드 추가
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

        // 뷰 바인딩
        binding = FragmentLogInBinding.inflate(inflater, container, false)

        // Retrofit 인스턴스 생성
        service = RetrofitConnection.getInstance().create(MemberService::class.java)

        // 로그인 버튼
        binding.btnLogin.setOnClickListener {
            binding.loginError.visibility = View.GONE
            login()
        }

        // 회원가입 버튼
        binding.btnSignUpPage.setOnClickListener {
            (requireActivity() as? MainActivity)?.replaceFragment(SignUpFragment(), R.id.fragment_my_closet)
        }

        // 텍스트 변경 감지
        addTextChangedListenerToEditTexts(binding.editId, binding.editPw)

        return binding.root
    }

    /**
     * 로그인
     */
    private fun login() {
        // 아이디, 비밀번호 받기
        val loginId = binding.editId.text.toString()
        val password = binding.editPw.text.toString()

        if (loginId == null || password == null) {
            binding.loginError.text = "아이디 또는 비밀번호를 확인하세요"
        }

        val loginRequest = LoginRequestDTO(loginId, password)

        // 로그인 요청 보내기
        service.login(loginRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // 요청 성공
                if (response.isSuccessful) {
                    // access/refresh 토큰 저장
                    val accessToken = response.headers()["Authorization"]
                    val refreshToken = extractRefreshToken(response.headers()["Set-Cookie"])

                    if (accessToken != null && refreshToken != null) {
                        MainApplication.prefs.saveAccessToken(accessToken)
                        MainApplication.prefs.saveRefreshToken(refreshToken)
                    }

                    showToast("로그인 성공! 환영합니다.")

                    // 로그인 후 메인 페이지로 이동
                    (requireActivity() as? MainActivity)?.replaceFragment(HomeFragment(), R.id.fragment_home)

                // 요청 실패
                } else {
                    // 로그인 에러 처리
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val errorJson = JSONObject(errorBody)
                            val error = errorJson.optString("error")

                            if (error == "Unauthorized") {
                                binding.loginError.text = "아이디 또는 비밀번호를 확인하세요"
                                binding.loginError.visibility = View.VISIBLE
                            } else {
                                showToast("로그인 실패")
                                Log.e("Login", "로그인 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                            }
                        } catch (e: JSONException) {
                            showToast("로그인 실패")
                            Log.e("Login", "로그인 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                        }
                    } else {
                        showToast("로그인 실패")
                        Log.e("Login", "로그인 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                    }
                }
            }

            // 요청 실패
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
            }
        })
    }

    /**
     * 에러메시지 노출 후 텍스트 변경 감지
     */
    private fun addTextChangedListenerToEditTexts(vararg editTexts: EditText) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.loginError.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        for (editText in editTexts) {
            editText.addTextChangedListener(textWatcher)
        }
    }

    /**
     * 토큰 추출
     */
    private fun extractRefreshToken(setCookieHeader: String?): String? {
        setCookieHeader?.split(";")?.forEach { cookie ->
            val parts = cookie.split("=")
            if (parts.size == 2 && parts[0].trim() == "refresh") {
                return parts[1].trim()
            }
        }
        return null
    }

    /**
     * 토스트 메시지 띄우기
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}