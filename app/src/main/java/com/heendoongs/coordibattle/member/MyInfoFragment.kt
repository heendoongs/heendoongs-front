package com.heendoongs.coordibattle.member

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.coordi.HomeFragment
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.databinding.FragmentMyInfoBinding
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 마이페이지_내정보 프래그먼트
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

class MyInfoFragment : Fragment() {

    private lateinit var service: MemberService
    private lateinit var binding: FragmentMyInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyInfoBinding.inflate(inflater, container, false)

        service = RetrofitConnection.getInstance().create(MemberService::class.java)

        binding.btnUpdate.setOnClickListener {
            update()
        }

        binding.btnDelete.setOnClickListener {
            delete()
        }

        messageInit()
        getMyInfo()

        return binding.root
    }

    private fun getMyInfo() {
        service.getMyInfo().enqueue(object : Callback<MyInfoResponse> {
            override fun onResponse(call: Call<MyInfoResponse>, response: Response<MyInfoResponse>) {
                if (response.isSuccessful) {
                    val myInfoResponse = response.body()
                    if (myInfoResponse != null) {
                        binding.editId.text = myInfoResponse.loginId
                        binding.editNickname.setText(myInfoResponse.nickname)
                    } else {
                        showToast("데이터를 가져올 수 없습니다.")
                    }
                } else {
                    showToast("데이터를 가져올 수 없습니다.")
                }
            }

            override fun onFailure(call: Call<MyInfoResponse>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    private fun update() {
        val password = binding.editPw.text.toString()
        val passwordCheck = binding.editPwChk.text.toString()
        val nickname = binding.editNickname.text.toString()

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

        val updateRequest = MemberUpdateRequest(password, nickname)

        // 회원정보 수정 요청 보내기
        service.updateAccount(updateRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("회원 정보 수정 완료")
                    messageInit()
                } else {
                    // 회원가입 실패 처리
                    val errorBody = response.errorBody()?.string()
                    val exceptionDto = Gson().fromJson(errorBody, ExceptionDto::class.java)

                    // 에러 유형에 따라 메시지 표시
                    when (exceptionDto.code) {
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

    private fun delete() {

        // 회원 탈퇴 요청 보내기
        service.deleteAccount().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("회원 탈퇴 완료")
                    val mainActivity = activity as? MainActivity
                    mainActivity?.getPreferenceUtil()?.clearTokens()
                    (requireActivity() as? MainActivity)?.replaceFragment(LogInFragment())
                } else {
                    showToast("회원 탈퇴 오류")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    // 에러 메시지 초기화
    private fun messageInit() {
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

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}