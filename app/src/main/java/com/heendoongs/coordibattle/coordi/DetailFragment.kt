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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.*
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.RetrofitConnection
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
 * </pre>
 */

class DetailFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var service: CoordiService
    private lateinit var updateButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var checkButton: ImageButton
    private lateinit var xButton: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var titleEditText: EditText
    private var memberId: Long = 6002L // 실제 데이터로 교체 필요
    private var coordiId: Long? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false)
        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())
        service = RetrofitConnection.getInstance().create(CoordiService::class.java)
        coordiId = arguments?.getLong("coordiId")
        loadCoordiDetails()
        return rootView
    }

    private fun loadCoordiDetails() {
        coordiId?.let { id ->
            val call = service.getCoordiDetails(memberId, id)
            call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    if (response.isSuccessful) {
                        val coordiDetails = response.body()
                        coordiDetails?.let { data ->
                            titleTextView = rootView.findViewById(R.id.coordi_detail_title_text)
                            titleEditText = rootView.findViewById(R.id.coordi_detail_title_edit)
                            titleTextView.text = data.coordiTitle
                            titleEditText.setText(data.coordiTitle)

                            rootView.findViewById<TextView>(R.id.coordi_detail_nickname).text = data.nickname
                            rootView.findViewById<TextView>(R.id.coordi_detail_create_date).text = data.createDate.toString()
                            rootView.findViewById<TextView>(R.id.coordi_detail_vote_count).text = data.voteCount.toString()

                            val voteButton = rootView.findViewById<ImageButton>(R.id.coordi_detail_vote_button)
                            updateButton = rootView.findViewById(R.id.coordi_detail_update_button)
                            deleteButton = rootView.findViewById(R.id.coordi_detail_delete_button)
                            checkButton = rootView.findViewById(R.id.coordi_detail_check_button)
                            xButton  = rootView.findViewById(R.id.coordi_detail_x_button)

                            checkButton.visibility = View.INVISIBLE
                            xButton.visibility = View.INVISIBLE
                            checkButton.isClickable = false
                            xButton.isClickable = false

                            if (!data.isVotingPeriod) {
                                voteButton.setImageDrawable(context?.let {
                                    ContextCompat.getDrawable(
                                        it, R.drawable.coordi_detail_vote_disabled
                                    )
                                })
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
                            }

                            if (data.isCoordiPeriod && Objects.equals(memberId, data.memberId)) {
                                updateButton.setOnClickListener {
                                    toggleEditMode(true)
                                }
                                deleteButton.setOnClickListener {
                                    showDeleteDialog()
                                }
                                checkButton.setOnClickListener {
                                    updateTitle(memberId, id)
                                    toggleEditMode(false)
                                }
                                xButton.setOnClickListener {
                                    cancelEditMode()
                                }
                            } else{
                                updateButton.visibility = View.INVISIBLE;
                                deleteButton.visibility = View.INVISIBLE;
                                updateButton.isClickable = false
                                deleteButton.isClickable = false
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
                                    likeCoordi(memberId, id)
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {

                }
            })
        }
    }


    private fun likeCoordi(memberId: Long, coordiId: Long) {
        val call = service.likeCoordi(memberId, coordiId)
        call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
            override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                loadCoordiDetails()
            }

            override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {

            }
        })
    }

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

    private fun cancelEditMode() {
        titleEditText.setText(titleTextView.text)
        toggleEditMode(false)
    }

    private fun updateTitle(memberId: Long, coordiId: Long) {
        val newTitle = titleEditText.text.toString()
        if (newTitle != titleTextView.text.toString()) {
            val requestDTO = CoordiUpdateRequestDTO(newTitle)
            updateCoordi(memberId, coordiId, requestDTO)
        }
    }

    private fun updateCoordi(memberId: Long, coordiId: Long, requestDTO: CoordiUpdateRequestDTO){
        val call = service.updateCoordi(memberId, coordiId, requestDTO)
        call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
            override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "제목이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    loadCoordiDetails()
                }
            }

            override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {

            }
        })
    }

    private fun showDeleteDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null)
        val dialog = AlertDialog.Builder(requireContext()).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setView(dialogView)

        dialogView.findViewById<ImageButton>(R.id.dialog_ok_button).setOnClickListener {
            deleteCoordi(memberId, coordiId)
            dialog.dismiss()
        }
        dialogView.findViewById<ImageButton>(R.id.dialog_cancel_button).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteCoordi(memberId: Long, coordiId: Long?) {
        val call = service.deleteCoordi(memberId, coordiId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Toast.makeText(context, responseBody, Toast.LENGTH_SHORT).show()
                    val homeFragment = HomeFragment()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_container, homeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DetailFragment", "삭제 실패, error: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("DetailFragment", "삭제 요청 실패", t)
            }
        })
    }

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

    private fun setupRecyclerView(clothes: List<ClothDetailsResponseDTO>) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.clothes_recycler_view)
        val adapter = DetailClothesAdapter(clothes, requireContext()) { cloth ->
            logItemClick(cloth)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
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
}