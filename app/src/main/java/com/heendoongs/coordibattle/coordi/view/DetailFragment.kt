package com.heendoongs.coordibattle.coordi.view

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.heendoongs.coordibattle.common.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.coordi.dto.ClothDetailsResponseDTO
import com.heendoongs.coordibattle.coordi.dto.CoordiDetailsResponseDTO
import com.heendoongs.coordibattle.coordi.dto.CoordiUpdateRequestDTO
import com.heendoongs.coordibattle.coordi.service.CoordiService
import com.heendoongs.coordibattle.databinding.FragmentDetailBinding
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.member.view.LogInFragment
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * 상세 페이지 프래그먼트
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * 2024.07.31   남진수       상세페이지 조회
 * 2024.08.02   임원정       coordiId 연결
 * 2024.08.02   남진수       상세페이지 코디 수정기능
 * 2024.08.02   남진수       상세페이지 코디 삭제기능
 * 2024.08.02   남진수       코디 좋아요 기능
 * 2024.08.04   남진수       구글 애널리틱스 관련 설정 추가
 * 2024.08.06   남진수       ProgressBar 설정
 * </pre>
 */

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var service: CoordiService
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private var memberId: Long? = null
    private var coordiId: Long? = null

    /**
     * ViewBinding 초기화 및 서비스 설정
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        service = RetrofitConnection.getInstance().create(CoordiService::class.java)

        coordiId = arguments?.getLong("coordiId")
        memberId = MainActivity.prefs.getMemberId()

        loadCoordiDetails()

        return binding.root
    }

    /**
     * 상세 페이지 불러오기
     */
    private fun loadCoordiDetails() {
        coordiId?.let { id ->
            binding.progressBar.visibility = View.VISIBLE
            service.getCoordiDetails(id).enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        response.body()?.let { data ->
                            populateDetailData(data)
                            setupButtons(data)
                            setupRecyclerView(data.clothesList)
                        }
                    } else {
                        showToast("상세 페이지 로드 실패")
                        Log.e("DetailFragment", "loadCoordiDetails 실패: ${response.errorBody()?.string()}")
                    }
                }
                override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    showToast("상세 페이지 로드 실패")
                    Log.e("DetailFragment", "loadCoordiDetails 네트워크 요청 실패", t)
                }
            })
        }
    }

    /**
     * 코디 데이터 UI에 설정
     */
    private fun populateDetailData(data: CoordiDetailsResponseDTO) {
        with(binding) {
            coordiDetailTitleText.text = data.coordiTitle
            coordiDetailTitleEdit.setText(data.coordiTitle)
            coordiDetailNickname.text = data.nickname
            coordiDetailCreateDate.text = data.createDate.toLocalDate().toString()
            coordiDetailVoteCount.text = data.voteCount.toString()
            binding.coordiDetailVoteHeart.visibility = View.VISIBLE

            val bitmap = decodeBase64ToBitmap(data.coordiImage)
            bitmap?.let {
                Glide.with(this@DetailFragment).load(it).into(coordiDetailImage)
            }

            if (!data.isVotingPeriod) {
                coordiDetailVoteButtonDisabled.setImageDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.coordi_detail_vote_disabled)
                )
                coordiDetailVoteButtonDisabled.visibility = View.VISIBLE
            } else {
                coordiDetailVoteButton.apply {
                    setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (data.isVoted) R.drawable.coordi_detail_vote_voted else R.drawable.coordi_detail_vote_not_voted
                        )
                    )
                    visibility = View.VISIBLE
                    setOnClickListener {
                        handleVoteClick(data)
                    }
                }
            }
        }
    }

    /**
     * 수정, 삭제, 저장 버튼 상태 설정
     */
    private fun setupButtons(data: CoordiDetailsResponseDTO) {
        with(binding) {
            if (data.isCoordiPeriod && Objects.equals(memberId, data.memberId)) {
                coordiDetailUpdateButton.visibility = View.VISIBLE
                coordiDetailDeleteButton.visibility = View.VISIBLE

                coordiDetailUpdateButton.setOnClickListener { toggleEditMode(true) }
                coordiDetailDeleteButton.setOnClickListener { showDeleteDialog() }
                coordiDetailCheckButton.setOnClickListener {
                    coordiId?.let { it1 -> updateCoordi(it1) }
                    toggleEditMode(false)
                }
                coordiDetailXButton.setOnClickListener { cancelEditMode() }
            }
        }
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView(clothesList: List<ClothDetailsResponseDTO>) {
        val clothes = clothesList.map {
            ClothDetailsResponseDTO(it.clothId, it.brand, it.productName, it.price, it.clothImageURL, it.productURL)
        }

        binding.clothesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = DetailClothesAdapter(clothes, requireContext()) { cloth ->
                logItemClick(cloth)
            }
        }
    }

    /**
     * 수정 모드 토글
     */
    private fun toggleEditMode(isEditMode: Boolean) {
        with(binding) {
            if (isEditMode) {
                coordiDetailTitleText.visibility = View.INVISIBLE
                coordiDetailTitleEdit.visibility = View.VISIBLE
                coordiDetailUpdateButton.visibility = View.INVISIBLE
                coordiDetailDeleteButton.visibility = View.INVISIBLE
                coordiDetailCheckButton.visibility = View.VISIBLE
                coordiDetailXButton.visibility = View.VISIBLE
            } else {
                coordiDetailTitleText.visibility = View.VISIBLE
                coordiDetailTitleEdit.visibility = View.INVISIBLE
                coordiDetailUpdateButton.visibility = View.VISIBLE
                coordiDetailDeleteButton.visibility = View.VISIBLE
                coordiDetailCheckButton.visibility = View.INVISIBLE
                coordiDetailXButton.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * 수정 모드 취소
     */
    private fun cancelEditMode() {
        binding.coordiDetailTitleEdit.setText(binding.coordiDetailTitleText.text)
        toggleEditMode(false)
    }

    /**
     * 상세페이지 수정
     */
    private fun updateCoordi(coordiId: Long) {
        val newTitle = binding.coordiDetailTitleEdit.text.toString()
        if (newTitle != binding.coordiDetailTitleText.text.toString()) {
            val requestDTO = CoordiUpdateRequestDTO(newTitle)
            service.updateCoordi(coordiId, requestDTO).enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            showToast("제목이 수정되었습니다!")
                            binding.coordiDetailTitleText.text = it.coordiTitle
                            binding.coordiDetailTitleEdit.setText(it.coordiTitle)
                        }
                    } else {
                        handleUpdateError(response)
                    }
                }
                override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                    showToast("제목 수정에 실패했습니다.")
                    Log.e("DetailFragment", "updateCoordi 네트워크 요청 실패", t)
                }
            })
        }
    }

    /**
     * 수정 오류 처리
     */
    private fun handleUpdateError(response: Response<CoordiDetailsResponseDTO>) {
        val errorBody = response.errorBody()?.string()
        val errorMap = errorBody?.let {
            Gson().fromJson<Map<String, String>>(it, object : TypeToken<Map<String, String>>() {}.type)
        }

        val errorMessage = errorMap?.get("message") ?: "제목은 1자 이상 15자 이하로 작성해 주세요."
        showToast(errorMessage)
        Log.e("DetailFragment", "updateCoordi 실패: $errorMessage")
    }

    /**
     * 삭제 다이얼로그
     */
    private fun showDeleteDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null)
        val dialog = AlertDialog.Builder(requireContext()).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setView(dialogView)

        dialogView.findViewById<ImageButton>(R.id.dialog_ok_button).setOnClickListener {
            deleteCoordi(coordiId)
            dialog.dismiss()
        }
        dialogView.findViewById<ImageButton>(R.id.dialog_cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    /**
     * 상세페이지 삭제
     */
    private fun deleteCoordi(coordiId: Long?) {
        service.deleteCoordi(coordiId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    showToast(responseBody)
                    navigateToHome()
                } else {
                    Log.e("DetailFragment", "코디 삭제 실패, error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("DetailFragment", "삭제 요청 실패", t)
            }
        })
    }

    /**
     * 상세페이지 내 코디 투표(좋아요)
     */
    private fun handleVoteClick(data: CoordiDetailsResponseDTO) {
        if (data.isVotingPeriod) {
            if (memberId == -1L || memberId == null) {
                showToast("로그인 이후 사용해주세요")
                navigateToLogin()
            } else {
                coordiId?.let { likeCoordi(it) }
            }
        }
    }

    /**
     * 코디 좋아요 기능
     */
    private fun likeCoordi(coordiId: Long) {
        service.likeCoordi(coordiId).enqueue(object : Callback<CoordiDetailsResponseDTO> {
            override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        binding.coordiDetailVoteButton.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                if (it.isVoted) R.drawable.coordi_detail_vote_voted else R.drawable.coordi_detail_vote_not_voted
                            )
                        )
                        binding.coordiDetailVoteCount.text = it.voteCount.toString()
                    }
                } else {
                    showToast("코디 투표에 실패했습니다.")
                    Log.e("DetailFragment", "likeCoordi 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                showToast("코디 투표에 실패했습니다.")
                Log.e("DetailFragment", "likeCoordi 네트워크 요청 실패", t)
            }
        })
    }

    /**
     * Google Analytics 설정
     */
    private fun logItemClick(cloth: ClothDetailsResponseDTO) {
        val bundle = Bundle().apply {
            putString("cloth_id", cloth.clothId.toString())
            putString("cloth_brand", cloth.brand)
            putString("cloth_name", cloth.productName)
            putString("coordi_id", coordiId.toString())
        }
        firebaseAnalytics.logEvent("cloth_item_click", bundle)
        Log.d("FirebaseAnalytics", "Event logged: cloth_item_click - ${cloth.clothId}")
    }

    /**
     * Base64 문자열 디코딩
     */
    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val base64Data = base64Str.substringAfter(",")
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        val homeFragment = HomeFragment()
        (activity as MainActivity).replaceFragment(homeFragment, R.id.fragment_my_closet)
    }

    private fun navigateToLogin() {
        val loginFragment = LogInFragment()
        (activity as MainActivity).replaceFragment(loginFragment, R.id.fragment_my_closet)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}