package com.heendoongs.coordibattle.member

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.battle.BattleService
import com.heendoongs.coordibattle.coordi.*
import com.heendoongs.coordibattle.global.RetrofitConnection
import com.heendoongs.coordibattle.databinding.FragmentMyClosetBinding
import com.heendoongs.coordibattle.global.checkLoginAndNavigate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * 마이페이지_내 옷장 프래그먼트
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * 2024.07.31  	조희정       getMyCloset, logout 메소드 생성
 * </pre>
 */

class MyClosetFragment :Fragment(), CoordiAdapter.OnItemClickListener {

    private lateinit var service: MemberService
    private lateinit var binding: FragmentMyClosetBinding

    private lateinit var adapter: CoordiAdapter
    private var currentPage = 0
    private val pageSize = 6

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyClosetBinding.inflate(inflater, container, false)

        if (!checkLoginAndNavigate()) {
            return binding.root
        }

        // Retrofit 인스턴스 생성 시 토큰 포함
        service = RetrofitConnection.getInstance().create(MemberService::class.java)

        // RecyclerView 초기화
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2) // 한 행에 2개 아이템 표시

        // 어댑터 초기화 및 설정
        adapter = CoordiAdapter(requireContext(), mutableListOf(), this)
        binding.recyclerView.adapter = adapter

        binding.btnMyInfoPage.setOnClickListener {
            (requireActivity() as? MainActivity)?.replaceFragment(MyInfoFragment())
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        loadNickname()
        loadMyCloset(currentPage, pageSize)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (!checkLoginAndNavigate()) {
            return
        }
    }

    private fun loadNickname() {
        service.getNickname().enqueue(object : Callback<MyNicknameResponse> {
            override fun onResponse(call: Call<MyNicknameResponse>, response: Response<MyNicknameResponse>) {
                if (response.isSuccessful) {
                    // 성공적인 응답 처리
                    val myClosetResponse = response.body()
                    println(response.body()!!.nickname)
                    if (myClosetResponse != null) {
                        binding.nickname.text = myClosetResponse.nickname
                        binding.myCoordiList.text = myClosetResponse.nickname + "의 옷장"
                    } else {
                        showToast("데이터를 가져올 수 없습니다.")
                    }
                } else {
                    println("#@#@#@#" + response.message())
                    showToast("데이터 가져오기 실패. 상태 코드: ${response.code()}, 메시지: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<MyNicknameResponse>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. 다시 시도하세요.")
            }
        })
    }

    private fun loadMyCloset(page: Int, size: Int) {
        service.getMyClosetList(page, size).enqueue(object : Callback<Page<CoordiListResponseDTO>> {
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
                    Toast.makeText(context, "222 Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Page<CoordiListResponseDTO>>, t: Throwable) {
                Toast.makeText(context, "222 Error connecting to the server: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun logout() {
        val mainActivity = activity as? MainActivity
        mainActivity?.getPreferenceUtil()?.clearTokens()
        showToast("로그아웃 되었습니다.")
        mainActivity?.replaceFragment(LogInFragment())
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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
}