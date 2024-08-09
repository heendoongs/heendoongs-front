package com.heendoongs.coordibattle.battle.view

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.battle.dto.BannerResponseDTO
import com.heendoongs.coordibattle.common.MainActivity
import com.heendoongs.coordibattle.coordi.view.CoordiFragment
import com.heendoongs.coordibattle.databinding.ItemBannerBinding
import com.smarteist.autoimageslider.SliderViewAdapter

/**
 * 배너 슬라이더 어댑터
 * @author 임원정
 * @since 2024.07.31
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.31  	임원정       최초 생성
 * </pre>
 */

class BannerSliderAdapter(private val banners: List<BannerResponseDTO>, private val context: Context) :
    SliderViewAdapter<BannerSliderAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: BannerViewHolder, position: Int) {
        val banner = banners[position]
        with(viewHolder.binding) {
            bannerTitle.text = banner.battleTitle
            bannerPeriod.text = "${banner.startDate.toString()} ~ ${banner.endDate.toString()}"
            // 배너 이미지 어둡게
            bannerImage.setColorFilter(Color.parseColor("#E3E3E3"), PorterDuff.Mode.MULTIPLY)

            Glide.with(context)
                .load(banner.bannerImageURL)
                .into(bannerImage)

            when (banner.periodType) {
                'C' -> {
                    bannerButton.text = "옷 입히기"
                    bannerImage.setOnClickListener {
                        val fragment = CoordiFragment()
                        val mainActivity = context as MainActivity
                        mainActivity.replaceFragment(fragment, R.id.fragment_coordi)
                    }
                    bannerButton.setOnClickListener {
                        val fragment = CoordiFragment()
                        val mainActivity = context as MainActivity
                        mainActivity.replaceFragment(fragment, R.id.fragment_coordi)
                    }
                }
                'V' -> {
                    bannerButton.text = "투표하기"
                    bannerImage.setOnClickListener {
                        val fragment = BattleEntranceFragment()
                        val mainActivity = context as MainActivity
                        mainActivity.replaceFragment(fragment, R.id.fragment_battle)
                    }
                    bannerButton.setOnClickListener {
                        val fragment = BattleEntranceFragment()
                        val mainActivity = context as MainActivity
                        mainActivity.replaceFragment(fragment, R.id.fragment_battle)
                    }
                }
            }
            bannerButton.visibility = View.VISIBLE
        }
    }

    override fun getCount(): Int {
        return banners.size
    }

    class BannerViewHolder(val binding: ItemBannerBinding) : SliderViewAdapter.ViewHolder(binding.root)
}