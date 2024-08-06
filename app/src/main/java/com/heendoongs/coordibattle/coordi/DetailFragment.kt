package com.heendoongs.coordibattle.coordi

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.member.LogInFragment
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
 * 2024.08.04   남진수       구글 애널리틱스 관련 설정 추가
 * 2024.08.06   남진수       ProgressBar 설정
 * </pre>
 */

class DetailFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var service: CoordiService
    private lateinit var updateButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var checkButton: ImageButton
    private lateinit var xButton: ImageButton
    private lateinit var voteButton: ImageButton
    private lateinit var voteDisabledButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var titleEditText: EditText
    private lateinit var voteCount: TextView
    private var memberId: Long? = null
    private var coordiId: Long? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        service = RetrofitConnection.getInstance().create(CoordiService::class.java)
        coordiId = arguments?.getLong("coordiId")
        progressBar = rootView.findViewById(R.id.progress_bar)

        memberId = MainActivity.prefs.getMemberId()

        loadCoordiDetails()
        return rootView
    }

    private fun loadCoordiDetails() {
        coordiId?.let { id ->
            progressBar.visibility = View.VISIBLE
            val call = service.getCoordiDetails(id)
            call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val coordiDetails = response.body()
                        coordiDetails?.let { data ->
                            titleTextView = rootView.findViewById(R.id.coordi_detail_title_text)
                            titleEditText = rootView.findViewById(R.id.coordi_detail_title_edit)
                            titleTextView.text = data.coordiTitle
                            titleEditText.setText(data.coordiTitle)

                            rootView.findViewById<TextView>(R.id.coordi_detail_nickname).text = data.nickname
                            rootView.findViewById<TextView>(R.id.coordi_detail_create_date).text = data.createDate.toLocalDate().toString()

                            voteCount = rootView.findViewById(R.id.coordi_detail_vote_count)
                            voteButton = rootView.findViewById(R.id.coordi_detail_vote_button)
                            voteDisabledButton = rootView.findViewById(R.id.coordi_detail_vote_button_disabled)
                            updateButton = rootView.findViewById(R.id.coordi_detail_update_button)
                            deleteButton = rootView.findViewById(R.id.coordi_detail_delete_button)
                            checkButton = rootView.findViewById(R.id.coordi_detail_check_button)
                            xButton  = rootView.findViewById(R.id.coordi_detail_x_button)

                            rootView.findViewById<ImageView>(R.id.coordi_detail_vote_heart).visibility = View.VISIBLE
                            voteCount.text = data.voteCount.toString()

                            if (!data.isVotingPeriod) {
                                voteDisabledButton.setImageDrawable(context?.let {
                                    ContextCompat.getDrawable(
                                        it, R.drawable.coordi_detail_vote_disabled
                                    )
                                })
                                voteDisabledButton.visibility = View.VISIBLE
                            } else {
                                if (data.isVoted) {
                                    voteButton.setImageDrawable(context?.let {
                                        ContextCompat.getDrawable(
                                            it, R.drawable.coordi_detail_vote_voted
                                        )
                                    })
                                } else {
                                    voteButton.setImageDrawable(context?.let {
                                        ContextCompat.getDrawable(
                                            it, R.drawable.coordi_detail_vote_not_voted
                                        )
                                    })
                                }
                                voteButton.visibility = View.VISIBLE
                            }

                            if (data.isCoordiPeriod && Objects.equals(memberId, data.memberId)) {
                                updateButton.visibility = View.VISIBLE;
                                deleteButton.visibility = View.VISIBLE;

                                updateButton.setOnClickListener {
                                    toggleEditMode(true)
                                }
                                deleteButton.setOnClickListener {
                                    showDeleteDialog()
                                }
                                checkButton.setOnClickListener {
                                    updateCoordi(id)
                                    toggleEditMode(false)
                                }
                                xButton.setOnClickListener {
                                    cancelEditMode()
                                }
                            }

                            val bitmap = decodeBase64ToBitmap(data.coordiImage)
                            bitmap?.let {
                                Glide.with(this@DetailFragment).load(it).into(rootView.findViewById(R.id.coordi_detail_image))
                            }

                            val clothes = data.clothesList.map {
                                ClothDetailsResponseDTO(it.clothId, it.brand, it.productName, it.price, it.clothImageURL, it.productURL)
                            }
                            setupRecyclerView(clothes)

                            voteButton.setOnClickListener {
                                if (data.isVotingPeriod) {
                                    if (memberId == -1L) {
                                        Toast.makeText(context, "로그인 이후 사용해주세요", Toast.LENGTH_SHORT).show()
                                        val loginFragment = LogInFragment()
                                        val mainActivity = activity as MainActivity
                                        mainActivity.replaceFragment(loginFragment, R.id.fragment_my_closet)
                                    } else {
                                        likeCoordi(id)
                                    }
                                }
                            }
                        }
                    } else{
                        Toast.makeText(context, "상세 페이지 로드 실패", Toast.LENGTH_SHORT).show()
                        Log.e("DetailFragment", "loadCoordiDetails 실패: ${response.errorBody()?.string()}")
                    }
                }
                override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                    Toast.makeText(context, "상세 페이지 로드 실패", Toast.LENGTH_SHORT).show()
                    Log.e("DetailFragment", "loadCoordiDetails 네트워크 요청 실패", t)
                }
            })
        }
    }

    /**
     * 상세페이지 내 코디 투표(좋아요)
     */
    private fun likeCoordi(coordiId: Long) {
        val call = service.likeCoordi(coordiId)
        call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
            override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                if (response.isSuccessful) {
                    if (response.body()?.isVoted == true) {
                        voteButton.setImageDrawable(context?.let {
                            ContextCompat.getDrawable(
                                it, R.drawable.coordi_detail_vote_voted
                            )
                        })
                    } else {
                        voteButton.setImageDrawable(context?.let {
                            ContextCompat.getDrawable(
                                it, R.drawable.coordi_detail_vote_not_voted
                            )
                        })
                    }
                    voteButton.visibility = View.VISIBLE
                    voteCount.text = response.body()!!.voteCount.toString()
                } else {
                    Toast.makeText(context, "코디 투표에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("DetailFragment", "likeCoordi 실패: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                Toast.makeText(context, "코디 투표에 실패했습니다.", Toast.LENGTH_SHORT).show()
                Log.e("DetailFragment", "likeCoordi 네트워크 요청 실패", t)
            }
        })
    }

    /**
     * 상세페이지 제목 수정모드 설정
     */
    private fun toggleEditMode(isEditMode: Boolean) {
        if (isEditMode) {
            titleTextView.visibility = View.INVISIBLE
            titleEditText.visibility = View.VISIBLE
            updateButton.visibility = View.INVISIBLE
            deleteButton.visibility = View.INVISIBLE
            checkButton.visibility = View.VISIBLE
            xButton.visibility = View.VISIBLE
            checkButton.isClickable = true
            xButton.isClickable = true
        } else {
            titleTextView.visibility = View.VISIBLE
            titleEditText.visibility = View.INVISIBLE
            updateButton.visibility = View.VISIBLE
            deleteButton.visibility = View.VISIBLE
            checkButton.visibility = View.INVISIBLE
            xButton.visibility = View.INVISIBLE
            checkButton.isClickable = false
            xButton.isClickable = false
        }
    }

    /**
     * 상세페이지 제목 수정 취소 (X 표시)
     */
    private fun cancelEditMode() {
        titleEditText.setText(titleTextView.text)
        toggleEditMode(false)
    }

    /**
     * 상세페이지 제목 업데이트
     */
    private fun updateCoordi(coordiId: Long) {
        val newTitle = titleEditText.text.toString()
        if (newTitle != titleTextView.text.toString()) {
            val requestDTO = CoordiUpdateRequestDTO(newTitle)
            val call = service.updateCoordi(coordiId, requestDTO)
            call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "제목이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                        titleTextView.text = response.body()?.coordiTitle
                        titleEditText.setText(response.body()?.coordiTitle)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        val errorMap = errorBody?.let {
                            val gson = Gson()
                            val type = object : TypeToken<Map<String, String>>() {}.type
                            gson.fromJson<Map<String, String>>(it, type)
                        }

                        val errorMessage = errorMap?.get("message") ?: "제목은 1자 이상 15자 이하로 작성해 주세요."
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e("DetailFragment", "updateCoordi 실패: $errorMessage")
                    }
                }
                override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                    Toast.makeText(context, "제목 수정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("DetailFragment", "updateCoordi 네트워크 요청 실패", t)
                }
            })
        }
    }

    /**
     * 삭제 다이얼로그 불러오기
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
        val call = service.deleteCoordi(coordiId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Toast.makeText(context, responseBody, Toast.LENGTH_SHORT).show()
                    val homeFragment = HomeFragment()
                    val mainActivity = activity as MainActivity
                    mainActivity.replaceFragment(homeFragment, R.id.fragment_my_closet)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DetailFragment", "코디 삭제 실패, error: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("DetailFragment", "삭제 요청 실패", t)
            }
        })
    }

    /**
     * 구글 애널리틱스 log
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
     * RecyclerView 불러오기
     */
    private fun setupRecyclerView(clothes: List<ClothDetailsResponseDTO>) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.clothes_recycler_view)
        val adapter = DetailClothesAdapter(clothes, requireContext()) { cloth ->
            logItemClick(cloth)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    /**
     * Base64 디코드
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
}