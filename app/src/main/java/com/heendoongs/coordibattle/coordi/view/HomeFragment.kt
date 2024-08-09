package com.heendoongs.coordibattle.coordi.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.battle.view.BannerSliderAdapter
import com.heendoongs.coordibattle.battle.dto.BannerResponseDTO
import com.heendoongs.coordibattle.battle.service.BattleService
import com.heendoongs.coordibattle.battle.dto.BattleTitleResponseDTO
import com.heendoongs.coordibattle.coordi.service.CoordiService
import com.heendoongs.coordibattle.coordi.dto.CoordiFilterRequestDTO
import com.heendoongs.coordibattle.coordi.dto.CoordiListResponseDTO
import com.heendoongs.coordibattle.coordi.dto.Page
import com.heendoongs.coordibattle.databinding.FragmentHomeBinding
import com.smarteist.autoimageslider.SliderView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 홈 프래그먼트
 * @author 임원정
 * @since 2024.07.28
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.28  	임원정       최초 생성
 * 2024.07.30   임원정       코디 리스트 출력
 * 2024.07.31   임원정       더보기 버튼 구현
 * </pre>
 */

class HomeFragment : Fragment(), CoordiAdapter.OnItemClickListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CoordiAdapter
    private lateinit var service: CoordiService
    private lateinit var battleService: BattleService
    private var currentPage = 0
    private val pageSize = 6
    private var selectedBattleId: Long? = null
    private var selectedSortOrder: String = "RANKING" // 기본 정렬 순서

    companion object {
        private const val KEY_SELECTED_BATTLE_ID = "selectedBattleId"
        private const val KEY_SELECTED_SORT_ORDER = "selectedSortOrder"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // 상태 복원
        if (savedInstanceState != null) {
            selectedBattleId = savedInstanceState.getLong(KEY_SELECTED_BATTLE_ID)
            selectedSortOrder = savedInstanceState.getString(KEY_SELECTED_SORT_ORDER, "RANKING")
        }

        // Retrofit 설정
        service = RetrofitConnection.getInstance().create(CoordiService::class.java)
        battleService = RetrofitConnection.getInstance().create(BattleService::class.java)

        // RecyclerView 초기화
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2) // 한 행에 2개 아이템 표시

        // 어댑터 초기화 및 설정
        adapter = CoordiAdapter(requireContext(), mutableListOf(), this)
        binding.recyclerView.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE

        // 더보기 버튼
        binding.btnMore.setOnClickListener {
            currentPage++
            loadCoordiList(currentPage, pageSize)
        }

        // 필터 및 정렬 스피너 설정
        setupFilterSpinner()
        setupSortSpinner()

        // 배너 데이터 로드
        loadBanners()

        // 상태 복원
        if (savedInstanceState != null) {
            val selectedBattlePosition = savedInstanceState.getInt("selectedBattlePosition", 0)
            val selectedSortOrderPosition = savedInstanceState.getInt("selectedSortOrderPosition", 0)
            binding.spinnerBattleFilter.setSelection(selectedBattlePosition)
            binding.spinnerSort.setSelection(selectedSortOrderPosition)
        }

        return view
    }

    private fun setupFilterSpinner() {
        // 배틀 필터 스피너 설정
        battleService.getBattleTitles().enqueue(object : Callback<List<BattleTitleResponseDTO>> {
            override fun onResponse(call: Call<List<BattleTitleResponseDTO>>, response: Response<List<BattleTitleResponseDTO>>) {
                if (!isAdded) return  // Fragment가 여전히 활성 상태인지 확인
                if (response.isSuccessful && response.body() != null) {
                    val battles = response.body()!!
                    val battleTitles = arrayOf("배틀별") + battles.map { it.title }.toTypedArray()

                    // 배열의 첫 번째 항목을 기본값으로 설정
                    val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, battleTitles)
                    adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
                    binding.spinnerBattleFilter.adapter = adapter

                    // 이전에 선택한 배틀이 있으면 해당 배틀로 설정
                    selectedBattleId?.let {
                        val selectedIndex = battles.indexOfFirst { it.battleId == selectedBattleId }
                        if (selectedIndex >= 0) {
                            binding.spinnerBattleFilter.setSelection(selectedIndex + 1)
                        }
                    }

                    binding.spinnerBattleFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedBattleId = if (position == 0) null else battles[position - 1].battleId
                            currentPage = 0
                            loadCoordiList(currentPage, pageSize) // 페이지를 0으로 초기화
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                } else {
                    Toast.makeText(context, "Failed to load battles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BattleTitleResponseDTO>>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(context, "Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupSortSpinner() {
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val adapter = ArrayAdapter(requireContext(), R.layout.item_spinner, sortOptions)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerSort.adapter = adapter

        // 기본값으로 "RANKING"을 선택
        binding.spinnerSort.setSelection(sortOptions.indexOf("랭킹순"))

        // 이전에 선택한 정렬이 있으면 해당 정렬로 설정
        if (selectedSortOrder == "RECENT") {
            binding.spinnerSort.setSelection(sortOptions.indexOf("최신순"))
        }

        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSortOrder = if (position == 0) "RANKING" else "RECENT"
                currentPage = 0
                loadCoordiList(currentPage, pageSize) // 페이지를 0으로 초기화
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
                if (!isAdded) return
                binding.progressBar.visibility = View.GONE
                binding.btnMore.visibility = View.VISIBLE
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
                if (!isAdded) return
                Toast.makeText(context, "Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // 배너 가져오기
    private fun loadBanners() {
        battleService.getCurrentBattles().enqueue(object : Callback<List<BannerResponseDTO>> {
            override fun onResponse(call: Call<List<BannerResponseDTO>>, response: Response<List<BannerResponseDTO>>) {
                if (!isAdded) return

                if (response.isSuccessful && response.body() != null) {
                    setupSlider(response.body()!!)
                } else {
                    Toast.makeText(context, "Failed to load banners", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BannerResponseDTO>>, t: Throwable) {
                if (!isAdded) return
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

    override fun onItemClick(item: CoordiListResponseDTO) {
        val bundle = Bundle().apply {
            putLong("coordiId", item.coordiId)
        }
        val detailFragment = DetailFragment().apply {
            arguments = bundle
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_SELECTED_BATTLE_ID, selectedBattleId ?: -1L)
        outState.putString(KEY_SELECTED_SORT_ORDER, selectedSortOrder)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            selectedBattleId = if (it.getLong(KEY_SELECTED_BATTLE_ID) == -1L) null else it.getLong(
                KEY_SELECTED_BATTLE_ID
            )
            selectedSortOrder = it.getString(KEY_SELECTED_SORT_ORDER, "RANKING")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
