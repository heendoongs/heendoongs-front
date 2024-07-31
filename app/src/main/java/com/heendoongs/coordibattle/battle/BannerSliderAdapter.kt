package com.heendoongs.coordibattle.battle

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.battle.BannerResponseDTO
import com.heendoongs.coordibattle.battle.BattleFragment
import com.heendoongs.coordibattle.coordi.CoordiFragment
import com.smarteist.autoimageslider.SliderViewAdapter

class BannerSliderAdapter(private val banners: List<BannerResponseDTO>, private val context: Context) :
    SliderViewAdapter<BannerSliderAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup): BannerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
        return BannerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: BannerViewHolder, position: Int) {
        val banner = banners[position]
        viewHolder.bannerTitle.text = banner.bannerTitle
        viewHolder.bannerPeriod.text = "${banner.startDate} ~ ${banner.endDate}"

        Glide.with(context)
            .load(banner.bannerImageURL)
            .into(viewHolder.bannerImage)

        when (banner.periodType) {
            'C' -> {
                viewHolder.bannerActionButton.text = "옷 입히기"
                viewHolder.bannerActionButton.setOnClickListener {
                    val intent = Intent(context, CoordiFragment::class.java)
                    intent.putExtra("battleId", banner.bannerId)
                    context.startActivity(intent)
                }
            }
            'V' -> {
                viewHolder.bannerActionButton.text = "투표하기"
                viewHolder.bannerActionButton.setOnClickListener {
                    val intent = Intent(context, BattleFragment::class.java)
                    intent.putExtra("battleId", banner.bannerId)
                    context.startActivity(intent)
                }
            }
        }
        viewHolder.bannerActionButton.visibility = View.VISIBLE
    }

    override fun getCount(): Int {
        return banners.size
    }

    class BannerViewHolder(itemView: View) : SliderViewAdapter.ViewHolder(itemView) {
        val bannerImage: ImageView = itemView.findViewById(R.id.bannerImage)
        val bannerTitle: TextView = itemView.findViewById(R.id.bannerTitle)
        val bannerPeriod: TextView = itemView.findViewById(R.id.bannerPeriod)
        val bannerActionButton: Button = itemView.findViewById(R.id.bannerActionButton)
    }
}
