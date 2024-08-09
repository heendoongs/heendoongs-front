package com.heendoongs.coordibattle.member.view

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.common.MainActivity
import com.heendoongs.coordibattle.databinding.DialogDeleteBinding
import com.heendoongs.coordibattle.databinding.FragmentMyInfoBinding
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.member.dto.ExceptionDTO
import com.heendoongs.coordibattle.member.dto.MemberUpdateRequestDTO
import com.heendoongs.coordibattle.member.dto.MyInfoResponseDTO
import com.heendoongs.coordibattle.member.service.MemberService
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
 * 2024.08.01  	조희정       loadMyInfo 메소드 추가
 * 2024.08.01  	조희정       update, delete 메소드 추가
 * 2024.08.01  	조희정       messageInit, showMessage 메소드 추가
 * 2024.08.05  	조희정       showDeleteDialog 메소드 추가
 * 2024.08.07  	조희정       addTextChangedListenerToEditTexts 메소드 추가
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

        // 뷰 바인딩
        binding = FragmentMyInfoBinding.inflate(inflater, container, false)

        // Retrofit 인스턴스
        service = RetrofitConnection.getInstance().create(MemberService::class.java)

        // 수정하기 버튼 클릭
        binding.btnUpdate.setOnClickListener {
            update()
        }

        // 삭제하기 버튼 클릭
        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }

        // 에러 메시지 초기화
        errorMessageInit()

        // 내 정보 불러오기
        loadMyInfo()

        // 닉네임 3글자 넘는지 여부 확인
        addNicknameTextWatcher()

        return binding.root
    }

    /**
     * 내 정보 불러오기
     */
    private fun loadMyInfo() {
        service.getMyInfo().enqueue(object : Callback<MyInfoResponseDTO> {
            override fun onResponse(call: Call<MyInfoResponseDTO>, responseDTO: Response<MyInfoResponseDTO>) {
                // 요청 성공
                if (responseDTO.isSuccessful) {
                    val myInfoResponse = responseDTO.body()
                    if (myInfoResponse != null) {
                        binding.editId.text = myInfoResponse.loginId
                        binding.editNickname.setText(myInfoResponse.nickname)
                    } else {
                        showToast("내 정보를 가져올 수 없습니다.")
                        Log.e("getMyInfo", "내 정보 데이터 가져오기 실패. 상태 코드: ${responseDTO.code()}, 메시지: ${responseDTO.message()}")
                    }
                } else {
                    showToast("데이터를 가져올 수 없습니다.")
                    Log.e("getMyInfo", "내 정보 데이터 가져오기 실패. 상태 코드: ${responseDTO.code()}, 메시지: ${responseDTO.message()}")
                }
            }

            override fun onFailure(call: Call<MyInfoResponseDTO>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
            }
        })
    }

    /**
     * 회원 정보 수정
     */
    private fun update() {
        val password = binding.editPw.text.toString()
        val passwordCheck = binding.editPwChk.text.toString()
        val nickname = binding.editNickname.text.toString()

        // 입력값 null 체크
        if (password.isEmpty()) {
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

        val updateRequest = MemberUpdateRequestDTO(password, nickname)

        // 회원정보 수정 요청
        service.updateAccount(updateRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                // 요청 성공
                if (response.isSuccessful) {
                    showToast("회원 정보 수정 완료!")
                    errorMessageInit()

                    // 내 옷장 화면으로 이동
                    (requireActivity() as? MainActivity)?.replaceFragment(MyClosetFragment(), R.id.fragment_my_closet)

                // 요청 실패
                } else {
                    // 회원 정보 수정 실패
                    val errorBody = response.errorBody()?.string()
                    val exceptionDto = Gson().fromJson(errorBody, ExceptionDTO::class.java)

                    // 에러 메시지
                    when (exceptionDto.code) {
                        602 -> showErrorMessage(binding.nicknameError, exceptionDto.message)
                        else -> showToast(exceptionDto.message)
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
            }
        })
    }

    /**
     * 탈퇴 확인 다이얼로그
     */
    private fun showDeleteDialog() {
        val dialogBinding = DialogDeleteBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(requireContext()).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setView(dialogBinding.root)

        dialogBinding.dialogDeleteText.text = "정말 탈퇴하시겠습니까?"
        dialogBinding.dialogDeleteOk.text = "탈퇴하기"

        dialogBinding.dialogOkButton.setOnClickListener {
            delete()
            dialog.dismiss()
        }
        dialogBinding.dialogCancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * 회원 탈퇴
     */
    private fun delete() {
        service.deleteAccount().enqueue(object : Callback<ResponseBody> {
            // 회원 탈퇴 요청
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("탈퇴가 완료되었습니다. 또 만나요!")

                    // shared preference 내용 초기화
                    val mainActivity = activity as? MainActivity
                    mainActivity?.getPreferenceUtil()?.clearTokens()

                    // 홈 호면으로 이동
                    (requireActivity() as? MainActivity)?.replaceFragment(LogInFragment(), R.id.fragment_home)

                // 요청 실패
                } else {
                    showToast("회원 탈퇴 중 오류가 발생했습니다. 다시 시도해주세요")
                    Log.e("deleteAccount", "회원 탈퇴 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            // 요청 실패
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
            }
        })
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
                    binding.btnUpdate.isEnabled = false
                } else {
                    binding.nicknameError.visibility = View.GONE
                    binding.btnUpdate.isEnabled = true
                }
            }
        })
    }

    /**
     * 에러메시지 초기화
     */
    private fun errorMessageInit() {
        binding.nicknameError.visibility = View.GONE
        binding.pwError.visibility = View.GONE
    }

    /**
     * 에러메시지 보여주기
     */
    private fun showErrorMessage(visibleMessage: TextView, message: String) {
        errorMessageInit()

        visibleMessage.text = message
        visibleMessage.visibility = View.VISIBLE
    }

    /**
     * 토스트 메시지 띄우기
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}