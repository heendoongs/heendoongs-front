package com.heendoongs.coordibattle.battle.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.common.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.battle.dto.BattleRequestDTO
import com.heendoongs.coordibattle.battle.dto.MemberCoordiVoteRequestDTO
import com.heendoongs.coordibattle.battle.service.BattleService
import com.heendoongs.coordibattle.coordi.view.CoordiEntranceFragment
import com.heendoongs.coordibattle.databinding.FragmentBattleBinding
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.global.checkLoginAndNavigate
import com.heendoongs.coordibattle.member.view.LogInFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 배틀 프래그먼트
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * 2024.07.30  	남진수       배틀 페이지 리스트 조회
 * 2024.07.30  	남진수       배틀 투표 기능 추가
 * 2024 08 04   조희정       로그인 체크 메소드 추가
 * 2024.08.06  	남진수       ProgressBar 추가
 * 2024.08.06   임원정       상태바 색 변경 추가
 * </pre>
 */

class BattleFragment : Fragment() {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private lateinit var service: BattleService
    private var isClickable: Boolean = true
    private var memberId: Long? = null

    /**
     * ViewBinding 초기화 및 서비스 설정
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)

        requireActivity().window.statusBarColor = Color.parseColor("#FFF6DE")


        if (!checkLoginAndNavigate()) {
            return binding.root
        }

        memberId = MainActivity.prefs.getMemberId()
        service = RetrofitConnection.getInstance().create(BattleService::class.java)

        loadBattleData()

        return binding.root
    }

    /**
     * 로그인 상태를 확인
     */
    override fun onResume() {
        super.onResume()
        if (!checkLoginAndNavigate()) {
            Toast.makeText(requireContext(), "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
            return
        }
    }

    /**
     * 배틀 페이지 불러오기
     */
    private fun loadBattleData() {
        if (memberId != -1L) {
            service.getBattleCoordies().enqueue(object : Callback<List<BattleRequestDTO>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<List<BattleRequestDTO>>, response: Response<List<BattleRequestDTO>>) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val battleDataList = response.body()
                        battleDataList?.let { data ->
                            if (data.size >= 2) {
                                setupBattleView(data)
                            } else {
                                showEndOfBattleScreen()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<BattleRequestDTO>>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                }
            })
        } else {
            Toast.makeText(context, "로그인 이후 사용해주세요", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    /**
     * 배틀 화면 설정
     */
    private fun setupBattleView(data: List<BattleRequestDTO>) {
        val firstCoordi = data[0]
        val secondCoordi = data[1]

        binding.parentLayout.isVisible = true
        binding.battleDescriptionTopText.text = firstCoordi.coordiTitle
        binding.battleDescriptionTopName.text = "by. " + firstCoordi.nickname
        binding.battleDescriptionBottomText.text = secondCoordi.coordiTitle
        binding.battleDescriptionBottomName.text = "by. " + secondCoordi.nickname

        loadImage(firstCoordi.coordiImage, binding.battleImageTop)
        loadImage(secondCoordi.coordiImage, binding.battleImageBottom)

        isClickable = true

        binding.battleLayoutTop.setOnClickListener {
            if (isClickable) {
                isClickable = false
                postBattleResult(firstCoordi.coordiId, secondCoordi.coordiId)
                animateImageAndRefresh(binding.battleImageTop)
            }
        }

        binding.battleLayoutBottom.setOnClickListener {
            if (isClickable) {
                isClickable = false
                postBattleResult(secondCoordi.coordiId, firstCoordi.coordiId)
                animateImageAndRefresh(binding.battleImageBottom)
            }
        }
    }

    /**
     * 배틀이 끝난 화면을 표시합니다.
     */
    private fun showEndOfBattleScreen() {
        binding.parentLayout.isVisible = false
        binding.endOfBattleScreen.isVisible = true
        binding.endOfBattleBtn.setOnClickListener {
            val coordiFragment = CoordiEntranceFragment()
            val mainActivity = activity as MainActivity
            mainActivity.replaceFragment(coordiFragment, R.id.fragment_coordi)
        }
    }

    /**
     * 이미지 불러오기
     */
    private fun loadImage(base64Image: String, imageView: ImageView) {
        val bitmap = decodeBase64ToBitmap(base64Image)
        bitmap?.let {
            Glide.with(this).load(it).into(imageView)
        }
    }

    /**
     * 배틀 결과 처리
     */
    private fun postBattleResult(winnerCoordiId: Long, loserCoordiId: Long) {
        val voteRequest = MemberCoordiVoteRequestDTO(winnerCoordiId, loserCoordiId)
        service.postBattleResult(voteRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "투표 완료!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "서버 응답 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "배틀 결과 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
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

    /**
     * 배틀 새로고침
     */
    private fun animateImageAndRefresh(imageView: ImageView) {
        val cardView = imageView.parent as CardView
        val darkBackground = binding.darkBackground

        darkBackground.visibility = View.VISIBLE
        darkBackground.alpha = 0f
        darkBackground.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        cardView.bringToFront()
        cardView.cardElevation = 100f

        val parentView = binding.parentLayout
        val parentCenterX = parentView.width / 2f
        val parentCenterY = parentView.height / 2f

        val imageViewLocation = IntArray(2)
        imageView.getLocationOnScreen(imageViewLocation)
        val imageViewCenterX = imageViewLocation[0] + imageView.width / 2f
        val imageViewCenterY = imageViewLocation[1] + imageView.height / 2f

        val deltaX = parentCenterX - imageViewCenterX
        val deltaY = parentCenterY - imageViewCenterY

        cardView.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .translationX(deltaX)
            .translationY(deltaY)
            .setDuration(500)
            .withEndAction {
                cardView.postDelayed({
                    resetCardView(cardView)
                    darkBackground.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            darkBackground.visibility = View.GONE
                        }
                        .start()

                    loadBattleData()
                }, 1500)
            }
            .start()
    }

    /**
     * 카드뷰의 애니메이션 초기화
     */
    private fun resetCardView(cardView: CardView) {
        cardView.scaleX = 1f
        cardView.scaleY = 1f
        cardView.translationX = 0f
        cardView.translationY = 0f
        cardView.cardElevation = 10f
    }

    private fun navigateToLogin() {
        val loginFragment = LogInFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, loginFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        requireActivity().window.statusBarColor = Color.WHITE
        super.onDestroyView()
        _binding = null
    }
}