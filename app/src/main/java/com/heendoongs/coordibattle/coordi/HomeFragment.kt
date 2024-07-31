package com.heendoongs.coordibattle.coordi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * 홈 프래그먼트
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * 2024.07.30   임원정       코디 리스트 조회 구현
 * </pre>
 */

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CoordiAdapter
    private lateinit var service: CoordiService
    private var currentPage = 0
    private val pageSize = 6

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // BASE_URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(CoordiService::class.java)

        // RecyclerView 초기화
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2) // 한 행에 2개 아이템 표시

        // 어댑터 초기화 및 설정
        adapter = CoordiAdapter(requireContext(), mutableListOf())
        binding.recyclerView.adapter = adapter

        // 더보기 버튼
        binding.btnMore.setOnClickListener {
            currentPage++
            loadCoordiList(currentPage, pageSize)
        }

        // 처음 데이터 로드
        loadCoordiList(currentPage, pageSize)

        return view
    }

    private fun loadCoordiList(page: Int, size: Int) {
        service.getCoordiList(page, size).enqueue(object : Callback<Page<RankingOrderCoordiListResponseDTO>> {
            override fun onResponse(call: Call<Page<RankingOrderCoordiListResponseDTO>>, response: Response<Page<RankingOrderCoordiListResponseDTO>>) {
                if (response.isSuccessful && response.body() != null) {
                    val pageData = response.body()!!
                    val newItems = pageData.content
                    adapter.appendData(newItems)

                    // 데이터가 더 이상 없을 때 버튼 숨기기
                    if (page >= pageData.totalPages - 1) {
                        binding.btnMore.visibility = View.GONE
                    } else {
                        binding.btnMore.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Page<RankingOrderCoordiListResponseDTO>>, t: Throwable) {
                Toast.makeText(context, "Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}