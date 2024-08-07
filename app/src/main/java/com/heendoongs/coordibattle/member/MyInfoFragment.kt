package com.heendoongs.coordibattle.member

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.coordi.HomeFragment
import com.heendoongs.coordibattle.databinding.DialogDeleteBinding
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
            showDeleteDialog()
        }

        addTextChangedListenerToTextViews(binding.noPw, binding.existNickname, binding.pwNotMatch)
        addNicknameTextWatcher()
        loadMyInfo()

        return binding.root
    }

    private fun loadMyInfo() {
        service.getMyInfo().enqueue(object : Callback<MyInfoResponse> {
            override fun onResponse(call: Call<MyInfoResponse>, response: Response<MyInfoResponse>) {
                if (response.isSuccessful) {
                    val myInfoResponse = response.body()
                    if (myInfoResponse != null) {
                        binding.editId.text = myInfoResponse.loginId
                        binding.editNickname.setText(myInfoResponse.nickname)
                    } else {
                        showToast("내 정보를 가져올 수 없습니다.")
                        Log.e("getMyInfo", "내 정보 데이터 가져오기 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                    }
                } else {
                    showToast("데이터를 가져올 수 없습니다.")
                    Log.e("getMyInfo", "내 정보 데이터 가져오기 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MyInfoResponse>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
            }
        })
    }

    private fun update() {
        val password = binding.editPw.text.toString()
        val passwordCheck = binding.editPwChk.text.toString()
        val nickname = binding.editNickname.text.toString()

        if (password.isEmpty()) {
            binding.noPw.text = "비밀번호를 입력해주세요"
            return
        }

        if (passwordCheck.isEmpty()) {
            binding.pwNotMatch.text = "비밀번호 확인을 입력해주세요"
            return
        }

        // 비밀번호 확인
        if (password != passwordCheck) {
            binding.pwNotMatch.text = "비밀번호가 일치하지 않습니다."
            return
        }

        if (nickname.isEmpty()) {
            binding.existNickname.text = "닉네임을 입력해주세요"
            return
        }

        val updateRequest = MemberUpdateRequest(password, nickname)

        // 회원정보 수정 요청 보내기
        service.updateAccount(updateRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("회원 정보 수정 완료!")
                    (requireActivity() as? MainActivity)?.replaceFragment(MyClosetFragment(), R.id.fragment_my_closet)
                } else {
                    // 회원 정보 수정 실패
                    val errorBody = response.errorBody()?.string()
                    val exceptionDto = Gson().fromJson(errorBody, ExceptionDto::class.java)

                    // 에러 메시지
                    when (exceptionDto.code) {
                        602 -> binding.existNickname.text = exceptionDto.message
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

    private fun delete() {

        // 회원 탈퇴 요청 보내기
        service.deleteAccount().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showToast("탈퇴가 완료되었습니다. 또 만나요!")
                    val mainActivity = activity as? MainActivity
                    mainActivity?.getPreferenceUtil()?.clearTokens()
                    (requireActivity() as? MainActivity)?.replaceFragment(LogInFragment(), R.id.fragment_home)
                } else {
                    showToast("회원 탈퇴 중 오류가 발생했습니다. 다시 시도해주세요")
                    Log.e("deleteAccount", "회원 탈퇴 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

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
                    binding.existNickname.text = "닉네임은 3글자 이하로 입력해주세요."
                    binding.btnUpdate.isEnabled = false
                } else {
                    binding.existNickname.visibility = View.GONE
                    binding.btnUpdate.isEnabled = true
                }
            }
        })
    }

    /**
     * edittext에 입력 시 에러 메시지 삭제
     */
    private fun addTextChangedListenerToTextViews(vararg textViews: TextView) {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.noPw.visibility = View.GONE
                binding.pwNotMatch.visibility = View.GONE
                binding.existNickname.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        for (textView in textViews) {
            textView.addTextChangedListener(textWatcher)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}