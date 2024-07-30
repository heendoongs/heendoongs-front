package com.heendoongs.coordibattle.battle

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
 * </pre>
 */

class BattleFragment : Fragment() {

    private lateinit var service: BattleService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_battle, container, false)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(BattleService::class.java)

        loadBattleData(view)

        return view
    }

    private fun loadBattleData(view: View) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
//        val memberId = sharedPref?.getLong("USER_ID", 0L) ?: 0L
        val memberId = 2L

        if (memberId != 0L) {
            val call = service.getBattleCoordies(memberId)
            call.enqueue(object : Callback<List<BattleDTO>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<List<BattleDTO>>, response: Response<List<BattleDTO>>) {
                    if (response.isSuccessful) {
                        val battleDataList = response.body()
                        battleDataList?.let { data ->
                            if (data.size >= 2) {
                                val battleEx1 = view.findViewById<ImageView>(R.id.battle_ex_1)
                                val battleEx2 = view.findViewById<ImageView>(R.id.battle_ex_2)
                                val descriptionTopText = view.findViewById<TextView>(R.id.battle_description_top_text)
                                val descriptionTopName = view.findViewById<TextView>(R.id.battle_description_top_name)
                                val descriptionBottomText = view.findViewById<TextView>(R.id.battle_description_bottom_text)
                                val descriptionBottomName = view.findViewById<TextView>(R.id.battle_description_bottom_name)

                                val firstCoordi = data[0]
                                descriptionTopText.text = firstCoordi.coordiTitle
                                descriptionTopName.text = "by. " + firstCoordi.nickname

                                val bitmap1 = decodeBase64ToBitmap(firstCoordi.coordiImage)
                                bitmap1?.let {
                                    Glide.with(this@BattleFragment).load(it).into(battleEx1)
                                }

                                val secondCoordi = data[1]
                                descriptionBottomText.text = secondCoordi.coordiTitle
                                descriptionBottomName.text = "by. " + secondCoordi.nickname

                                val bitmap2 = decodeBase64ToBitmap(secondCoordi.coordiImage)
                                bitmap2?.let {
                                    Glide.with(this@BattleFragment).load(it).into(battleEx2)
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<List<BattleDTO>>, t: Throwable) {

                }
            })
        }
    }

    private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
}