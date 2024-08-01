package com.heendoongs.coordibattle.battle

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.coordi.CoordiFragment
import com.heendoongs.coordibattle.databinding.ItemBannerBinding
import com.smarteist.autoimageslider.SliderViewAdapter

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
            println(banner.battleTitle)
            bannerPeriod.text = "${banner.startDate} ~ ${banner.endDate}"

            Glide.with(context)
                .load(banner.bannerImageURL)
                .into(bannerImage)

            when (banner.periodType) {
                'C' -> {
                    bannerButton.text = "옷 입히기"
                    bannerImage.setOnClickListener {
                        val fragment = CoordiFragment()
                        val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.main_container, fragment)
                        fragmentTransaction.addToBackStack(null)
                        fragmentTransaction.commit()
                    }
                    bannerButton.setOnClickListener {
                        val fragment = CoordiFragment()
                        val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.main_container, fragment)
                        fragmentTransaction.addToBackStack(null)
                        fragmentTransaction.commit()
                    }
                }
                'V' -> {
                    bannerButton.text = "투표하기"
                    bannerImage.setOnClickListener {
                        val fragment = BattleFragment()
                        val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.main_container, fragment)
                        fragmentTransaction.addToBackStack(null)
                        fragmentTransaction.commit()
                    }
                    bannerButton.setOnClickListener {
                        val fragment = BattleFragment()
                        val fragmentTransaction = (context as FragmentActivity).supportFragmentManager.beginTransaction()
                        fragmentTransaction.replace(R.id.main_container, fragment)
                        fragmentTransaction.addToBackStack(null)
                        fragmentTransaction.commit()
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