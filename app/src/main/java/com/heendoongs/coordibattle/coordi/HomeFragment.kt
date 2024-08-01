package com.heendoongs.coordibattle.coordi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.heendoongs.coordibattle.RetrofitConnection
import com.heendoongs.coordibattle.battle.BannerSliderAdapter
import com.heendoongs.coordibattle.battle.BannerResponseDTO
import com.heendoongs.coordibattle.battle.BattleService
import com.heendoongs.coordibattle.databinding.FragmentHomeBinding
import com.smarteist.autoimageslider.SliderView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private lateinit var battleService: BattleService
    private var currentPage = 0
    private val pageSize = 6

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Retrofit 설정
        service = RetrofitConnection.getInstance().create(CoordiService::class.java)
        battleService = RetrofitConnection.getInstance().create(BattleService::class.java)

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

        // 배너 데이터 로드
        loadBanners()

        return view
    }

    private fun loadCoordiList(page: Int, size: Int) {
        service.getCoordiList(page, size).enqueue(object : Callback<Page<CoordiListResponseDTO>> {
            override fun onResponse(call: Call<Page<CoordiListResponseDTO>>, response: Response<Page<CoordiListResponseDTO>>) {
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

            override fun onFailure(call: Call<Page<CoordiListResponseDTO>>, t: Throwable) {
                Toast.makeText(context, "Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadBanners() {
        battleService.getCurrentBattles().enqueue(object : Callback<List<BannerResponseDTO>> {
            override fun onResponse(call: Call<List<BannerResponseDTO>>, response: Response<List<BannerResponseDTO>>) {
                if (response.isSuccessful && response.body() != null) {
                    setupSlider(response.body()!!)
                } else {
                    Toast.makeText(context, "Failed to load banners", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BannerResponseDTO>>, t: Throwable) {
                Toast.makeText(context, "Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
                println("Error connecting to the server: ${t.message}")
            }
        })
    }

    private fun setupSlider(banners: List<BannerResponseDTO>) {
        val sliderAdapter = BannerSliderAdapter(banners, requireContext())
        binding.banner.setSliderAdapter(sliderAdapter)
        binding.banner.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR)
        binding.banner.setScrollTimeInSec(3) // set scroll delay in seconds
        binding.banner.setAutoCycle(true)
        binding.banner.startAutoCycle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}