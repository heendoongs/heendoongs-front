package com.heendoongs.coordibattle.member

import android.content.Context
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
import com.heendoongs.coordibattle.databinding.FragmentMyClosetBinding
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

        // SharedPreferences에서 JWT 토큰과 memberId 가져오기
        val sharedPref = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("jwt_token", null)
        val memberId = sharedPref.getLong("memberId", -1)


        binding.btnUpdate.setOnClickListener {
            update(memberId)
        }

        binding.btnDelete.setOnClickListener {
            delete(memberId)
        }

        getMyCloset(memberId)

        return binding.root
    }

    private fun getMyCloset(memberId: Long) {
        service.getMyInfo(memberId).enqueue(object : Callback<MyInfoResponse> {
            override fun onResponse(call: Call<MyInfoResponse>, response: Response<MyInfoResponse>) {
                if (response.isSuccessful) {
                    // 성공적인 응답 처리
                    val myInfoResponse = response.body()
                    if (myInfoResponse != null) {
                        binding.id.text = myInfoResponse.loginId
                        binding.nickname.text = myInfoResponse.nickname
                    } else {
                        showToast("데이터를 가져올 수 없습니다.")
                    }
                } else {
                    // 실패한 응답 처리
                    showToast("데이터 가져오기 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MyInfoResponse>, t: Throwable) {
                // 요청 실패 처리
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    private fun update(memberId: Long) {
        val loginId = binding.editId.text.toString()
        val password = binding.editPw.text.toString()
        val passwordCheck = binding.editPwChk.toString()
        val nickname = binding.editNickname.text.toString()

        val updateRequest = MemberUpdateRequest(memberId, loginId, password, nickname)

        // 회원정보 수정 요청 보내기
        service.updateAccount(updateRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("회원 정보 수정 완료")
                } else {
                    showToast("회원 정보 수정 오류. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    private fun delete(memberId: Long) {

        // 회원 탈퇴 요청 보내
        service.deleteAccount(memberId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("회원 탈퇴 완료")
                } else {
                    showToast("회원 탈퇴 오. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
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