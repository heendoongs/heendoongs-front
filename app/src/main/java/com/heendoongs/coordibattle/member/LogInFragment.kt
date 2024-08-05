package com.heendoongs.coordibattle.member

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.MainApplication
import com.heendoongs.coordibattle.coordi.HomeFragment
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.databinding.FragmentLogInBinding
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
 * 2024.07.30  	로그인       login 메소드 추가
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
                    val accessToken = response.headers()["Authorization"]
                    val refreshToken = extractRefreshToken(response.headers()["Set-Cookie"])


                    if (accessToken != null && refreshToken != null) {
                        MainApplication.prefs.saveAccessToken(accessToken)
                        MainApplication.prefs.saveRefreshToken(refreshToken)
                    }

                    showToast("로그인 성공! 환영합니다.")
                    (requireActivity() as? MainActivity)?.replaceFragment(HomeFragment())
                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val errorJson = JSONObject(errorBody)
                            val error = errorJson.optString("error")
                            val errorMessage = errorJson.optString("message")

                            if (error == "Unauthorized" && errorMessage == "자격 증명에 실패하였습니다.") {
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

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 요청 실패 처리
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // 토큰 추출
    private fun extractRefreshToken(setCookieHeader: String?): String? {
        setCookieHeader?.split(";")?.forEach { cookie ->
            val parts = cookie.split("=")
            if (parts.size == 2 && parts[0].trim() == "refresh") {
                return parts[1].trim()
            }
        }
        return null
    }

}