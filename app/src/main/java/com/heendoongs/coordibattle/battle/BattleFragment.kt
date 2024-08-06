package com.heendoongs.coordibattle.battle

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.coordi.CoordiEntranceFragment
import com.heendoongs.coordibattle.coordi.CoordiFragment
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.global.checkLoginAndNavigate
import com.heendoongs.coordibattle.member.LogInFragment
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
 * </pre>
 */

class BattleFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var service: BattleService
    private var isClickable: Boolean = true
    private var memberId: Long? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_battle, container, false)

        requireActivity().window.statusBarColor = Color.parseColor("#FFF6DE")
        progressBar = rootView.findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        if (!checkLoginAndNavigate()) {
            return rootView
        }
        memberId = MainActivity.prefs.getMemberId()
        service = RetrofitConnection.getInstance().create(BattleService::class.java)

        loadBattleData()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (!checkLoginAndNavigate()) {
            Toast.makeText(requireContext(), "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun loadBattleData() {
        if (memberId != -1L) {
            val call = service.getBattleCoordies()
            call.enqueue(object : Callback<List<BattleDTO>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<List<BattleDTO>>, response: Response<List<BattleDTO>>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val battleDataList = response.body()
                        battleDataList?.let { data ->
                            if (data.size >= 2) {
                                val battleConstraintLayout = rootView.findViewById<ConstraintLayout>(R.id.parent_layout)
                                battleConstraintLayout.isVisible = true
                                val firstCoordi = data[0]
                                val secondCoordi = data[1]

                                rootView.findViewById<TextView>(R.id.battle_description_top_text).text = firstCoordi.coordiTitle
                                rootView.findViewById<TextView>(R.id.battle_description_top_name).text = "by. " + firstCoordi.nickname
                                rootView.findViewById<TextView>(R.id.battle_description_bottom_text).text = secondCoordi.coordiTitle
                                rootView.findViewById<TextView>(R.id.battle_description_bottom_name).text = "by. " + secondCoordi.nickname

                                loadImage(firstCoordi.coordiImage, R.id.battle_image_top)
                                loadImage(secondCoordi.coordiImage, R.id.battle_image_bottom)

                                isClickable = true

                                rootView.findViewById<LinearLayout>(R.id.battle_layout_top).setOnClickListener {
                                    if (isClickable) {
                                        isClickable = false
                                        postBattleResult(firstCoordi.coordiId, secondCoordi.coordiId)
                                        animateImageAndRefresh(rootView.findViewById(R.id.battle_image_top))
                                    }
                                }

                                rootView.findViewById<LinearLayout>(R.id.battle_layout_bottom).setOnClickListener {
                                    if (isClickable) {
                                        isClickable = false
                                        postBattleResult(secondCoordi.coordiId, firstCoordi.coordiId)
                                        animateImageAndRefresh(rootView.findViewById(R.id.battle_image_bottom))
                                    }
                                }
                            } else{
                                val battleConstraintLayout = rootView.findViewById<ConstraintLayout>(R.id.parent_layout)
                                battleConstraintLayout.isVisible = false
                                battleConstraintLayout.isClickable = false
                                val endOfBattleScreen = rootView.findViewById<ConstraintLayout>(R.id.end_of_battle_screen)
                                endOfBattleScreen.isVisible = true
                                val endOfBattleBtn = rootView.findViewById<ImageView>(R.id.end_of_battle_btn)
                                endOfBattleBtn.setOnClickListener {
                                    val coordiFragment = CoordiEntranceFragment()
                                    val mainActivity = activity as MainActivity
                                    mainActivity.replaceFragment(coordiFragment, R.id.fragment_coordi)
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<BattleDTO>>, t: Throwable) {

                }
            })
        } else {
            Toast.makeText(context, "로그인 이후 사용해주세요", Toast.LENGTH_SHORT).show()
            val loginFragment = LogInFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_container, loginFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun loadImage(base64Image: String, imageViewId: Int) {
        val imageView = rootView.findViewById<ImageView>(imageViewId)
        val bitmap = decodeBase64ToBitmap(base64Image)
        bitmap?.let {
            Glide.with(this).load(it).into(imageView)
        }
    }

    private fun postBattleResult(winnerCoordiId: Long, loserCoordiId: Long) {
        val voteRequest = MemberCoordiVoteRequestDTO(winnerCoordiId, loserCoordiId)
        val call = service.postBattleResult(voteRequest)
        call.enqueue(object : Callback<BattleResponseDTO> {
            override fun onResponse(call: Call<BattleResponseDTO>, response: Response<BattleResponseDTO>) {

            }

            override fun onFailure(call: Call<BattleResponseDTO>, t: Throwable) {

            }
        })
    }

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

    private fun animateImageAndRefresh(imageView: ImageView) {
        val cardView = imageView.parent as CardView
        val darkBackground = rootView.findViewById<View>(R.id.dark_background)

        darkBackground.visibility = View.VISIBLE
        darkBackground.alpha = 0f
        darkBackground.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        cardView.bringToFront()
        cardView.cardElevation = 100f

        val parentView = rootView.findViewById<ViewGroup>(R.id.parent_layout)
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

                    cardView.scaleX = 1f
                    cardView.scaleY = 1f
                    cardView.translationX = 0f
                    cardView.translationY = 0f
                    cardView.cardElevation = 10f

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

    override fun onDestroyView() {
        requireActivity().window.statusBarColor = Color.WHITE
        super.onDestroyView()
    }
}