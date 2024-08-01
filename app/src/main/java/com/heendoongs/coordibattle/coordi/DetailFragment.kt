package com.heendoongs.coordibattle.coordi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.*
import com.heendoongs.coordibattle.R
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
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * </pre>
 */

class DetailFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var service: CoordiService
    private var coordiId: Long = 1L // 실제 데이터로 교체 필요

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false)
        println(11111)

        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java,
                JsonDeserializer { json: JsonElement, type: Type?, jsonDeserializationContext: JsonDeserializationContext? ->
                    LocalDate.parse(
                        json.asJsonPrimitive.asString,
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                } as JsonDeserializer<LocalDate>)
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        println(2222)
        service = retrofit.create(CoordiService::class.java)
        println(3333)

        loadCoordiDetails()
        println(444)
        return rootView
    }

    private fun loadCoordiDetails() {
        println(5555)
        if (coordiId != 0L) {
            val call = service.getCoordiDetails(coordiId)
            println(6666)
            call.enqueue(object : Callback<CoordiDetailsResponseDTO> {
                override fun onResponse(call: Call<CoordiDetailsResponseDTO>, response: Response<CoordiDetailsResponseDTO>) {
                    println(7777)
                    if (response.isSuccessful) {
                        println(8888)
                        val coordiDetails = response.body()
                        coordiDetails?.let { data ->
                            rootView.findViewById<TextView>(R.id.coordi_detail_nickname).text = data.nickname
                            rootView.findViewById<TextView>(R.id.coordi_detail_create_date).text = data.createDate.toString()
                            rootView.findViewById<TextView>(R.id.coordi_detail_title).text = data.coordiTitle

                            println(data.nickname)
                            println(data.createDate.toString())
                            println(data.coordiTitle)

                            val bitmap = decodeBase64ToBitmap(data.coordiImage)
                            bitmap?.let {
                                Glide.with(this@DetailFragment).load(it).into(rootView.findViewById(R.id.coordi_detail_image))
                            }

                            // 의류 목록 설정
                            val clothes = data.clothesList.map {
                                ClothDetailsResponseDTO(it.clothId, it.brand, it.productName, it.price, it.clothImageURL, it.productURL)
                            }
                            setupRecyclerView(clothes)
                        }
                    }
                }

                override fun onFailure(call: Call<CoordiDetailsResponseDTO>, t: Throwable) {
                    // Handle failure
                    println("Failed to load coordi details: ${t.message}")
                }
            })
        }
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