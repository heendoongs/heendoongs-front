package com.heendoongs.coordibattle.coordi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.RetrofitConnection
import com.heendoongs.coordibattle.battle.BannerSliderAdapter
import com.heendoongs.coordibattle.battle.BannerResponseDTO
import com.heendoongs.coordibattle.battle.BattleService
import com.heendoongs.coordibattle.battle.BattleTitleResponseDTO
import com.heendoongs.coordibattle.databinding.FragmentHomeBinding
import com.smarteist.autoimageslider.SliderView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CoordiAdapter
    private lateinit var service: CoordiService
    private lateinit var battleService: BattleService
    private var currentPage = 0
    private val pageSize = 6
    private var selectedBattleId: Long? = null
    private var selectedSortOrder: String = "RANKING" // 기본 정렬 순서

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

        // 필터 및 정렬 스피너 설정
        setupFilterSpinner()
        setupSortSpinner()

        // 처음 데이터 로드
        loadCoordiList(currentPage, pageSize)

        // 배너 데이터 로드
        loadBanners()

        return view
    }

    private fun setupFilterSpinner() {
        // 배틀 필터 스피너 설정
        battleService.getCurrentBattles().enqueue(object : Callback<List<BannerResponseDTO>> {
            override fun onResponse(call: Call<List<BannerResponseDTO>>, response: Response<List<BannerResponseDTO>>) {
                if (response.isSuccessful && response.body() != null) {
                    val battles = response.body()!!
                    val battleTitles = arrayOf("배틀별") + battles.map { it.battleTitle }.toTypedArray()

                    // 배열의 첫 번째 항목을 기본값으로 설정
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, battleTitles)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerBattleFilter.adapter = adapter

                    binding.spinnerBattleFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedBattleId = if (position == 0) null else battles[position - 1].battleId
                            loadCoordiList(0, pageSize) // 페이지를 0으로 초기화
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // 선택된 항목이 없을 때 동작
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to load battles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BannerResponseDTO>>, t: Throwable) {
                Toast.makeText(context, "Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupSortSpinner() {
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sortOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = adapter

        // 기본값으로 "RANKING"을 선택
        binding.spinnerSort.setSelection(sortOptions.indexOf("랭킹순"))

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSortOrder = if (position == 0) "NEW" else "RANKING"
                loadCoordiList(0, pageSize) // 페이지를 0으로 초기화
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 선택된 항목이 없을 때 동작
            }
        }
    }

    private fun loadCoordiList(page: Int, size: Int) {
        val requestDTO = CoordiFilterRequestDTO(selectedBattleId, selectedSortOrder, page, size)

        // 필터가 적용된 경우, 필터 API 사용
        val call: Call<Page<CoordiListResponseDTO>> = if (selectedBattleId != null || selectedSortOrder != "RANKING") {
            service.getCoordiListWithFilter(requestDTO)
        } else {
            service.getCoordiList(page, size)
        }

        call.enqueue(object : Callback<Page<CoordiListResponseDTO>> {
            override fun onResponse(call: Call<Page<CoordiListResponseDTO>>, response: Response<Page<CoordiListResponseDTO>>) {
                if (response.isSuccessful && response.body() != null) {
                    val pageData = response.body()!!
                    val newItems = pageData.content
                    if (page == 0) {
                        adapter.updateData(newItems) // 초기 로드 시 데이터 설정
                    } else {
                        adapter.appendData(newItems) // 추가 데이터 로드 시
                    }

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
