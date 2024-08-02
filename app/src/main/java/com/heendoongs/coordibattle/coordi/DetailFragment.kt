package com.heendoongs.coordibattle.coordi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.*
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
 * </pre>
 */

class DetailFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var service: CoordiService
    //private var coordiId: Long = 1L // 실제 데이터로 교체 필요
    private var memberId: Long = 2L // 실제 데이터로 교체 필요

    private var coordiId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false)
        service = RetrofitConnection.getInstance().create(CoordiService::class.java)
        coordiId = arguments?.getLong("coordiId")
        loadCoordiDetails()
        return rootView
    }

    /*private fun loadCoordiDetails() {
        if (coordiId != 0L) {
            val call = service.getCoordiDetails(memberId, coordiId)
            call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    if (response.isSuccessful) {
                        val coordiDetails = response.body()
                        coordiDetails?.let { data ->
                            rootView.findViewById<TextView>(R.id.coordi_detail_nickname).text = data.nickname
                            rootView.findViewById<TextView>(R.id.coordi_detail_create_date).text = data.createDate.toString()
                            rootView.findViewById<TextView>(R.id.coordi_detail_title).text = data.coordiTitle

                            val voteButton = rootView.findViewById<ImageButton>(R.id.coordi_detail_vote_button)

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
    }*/

    private fun loadCoordiDetails() {
        coordiId?.let { id ->
            val call = service.getCoordiDetails(memberId, id)
            call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    if (response.isSuccessful) {
                        val coordiDetails = response.body()
                        coordiDetails?.let { data ->
                            rootView.findViewById<TextView>(R.id.coordi_detail_nickname).text = data.nickname
                            rootView.findViewById<TextView>(R.id.coordi_detail_create_date).text = data.createDate.toString()
                            rootView.findViewById<TextView>(R.id.coordi_detail_title).text = data.coordiTitle

                            val voteButton = rootView.findViewById<ImageButton>(R.id.coordi_detail_vote_button)

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

                            val bitmap = decodeBase64ToBitmap(data.coordiImage)
                            bitmap?.let {
                                Glide.with(this@DetailFragment).load(it).into(rootView.findViewById(R.id.coordi_detail_image))
                            }

                            val clothes = data.clothesList.map {
                                ClothDetailsResponseDTO(it.clothId, it.brand, it.productName, it.price, it.clothImageURL, it.productURL)
                            }
                            setupRecyclerView(clothes)

                            voteButton.setOnClickListener {
                                likeCoordi(data.memberId, id)
                            }
                        }
                    } else {
                        Toast.makeText(context, "Failed to load details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                    Toast.makeText(context, "Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
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