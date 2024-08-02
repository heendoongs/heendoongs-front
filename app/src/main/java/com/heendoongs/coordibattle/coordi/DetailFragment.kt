package com.heendoongs.coordibattle.coordi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.*
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.RetrofitConnection
import com.heendoongs.coordibattle.battle.BattleResponseDTO
import com.heendoongs.coordibattle.battle.MemberCoordiVoteRequestDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
 * 2024.08.02   남진수       상세능페이지 코디 수정기능
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
    private var coordiId: Long = 3L // 실제 데이터로 교체 필요
    private var memberId: Long = 2L // 실제 데이터로 교체 필요

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false)

        service = RetrofitConnection.getInstance().create(CoordiService::class.java)

        loadCoordiDetails()
        return rootView
    }

    private fun loadCoordiDetails() {
        if (coordiId != 0L) {
            val call = service.getCoordiDetails(memberId, coordiId)
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

                            if (data.isCoordiPeriod && memberId == data.memberId) {
                                updateButton.setOnClickListener {
                                    toggleEditMode(true)
                                }
                                deleteButton.setOnClickListener {

                                }
                                checkButton.setOnClickListener {
                                    updateTitle(data.memberId, coordiId)
                                    toggleEditMode(false)
                                }
                                xButton.setOnClickListener {
                                    cancelEditMode()
                                }
                            } else{
                                updateButton.visibility = View.INVISIBLE;
                                deleteButton.visibility = View.INVISIBLE;
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
                                likeCoordi(data.memberId, coordiId)
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
                loadCoordiDetails()
            }

            override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {

            }
        })
    }

    private fun deleteCoordi(memberId: Long, coordiId: Long){
        val call = service.deleteCoordi(memberId, coordiId)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                HomeFragment()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {

            }
        })
    }

    private fun setupRecyclerView(clothes: List<ClothDetailsResponseDTO>) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.clothes_recycler_view)
        recyclerView.adapter = DetailClothesAdapter(clothes)
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