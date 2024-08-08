package com.heendoongs.coordibattle.coordi.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.coordi.dto.ClothDetailsResponseDTO
import java.text.DecimalFormat

/**
 * 상세 페이지 어댑터
 * @author 남진수
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	남진수       최초 생성
 * 2024.07.31   남진수       상세페이지 조회
 * 2024.08.04   남진수       구글 애널리틱스 관련 설정 추가
 * </pre>
 */

class DetailClothesAdapter(
    private val clothes: List<ClothDetailsResponseDTO>,
    private val context: Context,
    private val logItemClick: (ClothDetailsResponseDTO) -> Unit
) : RecyclerView.Adapter<DetailClothesAdapter.ClothesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cloth_image, parent, false)
        return ClothesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothesViewHolder, position: Int) {
        val cloth = clothes[position]
        holder.bind(cloth, context, logItemClick)
    }

    override fun getItemCount(): Int = clothes.size

    /**
     * 상세 페이지 RecyclerView 내부 데이터
     */
    class ClothesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.cloth_image)
        private val brandView: TextView = itemView.findViewById(R.id.cloth_brand)
        private val nameView: TextView = itemView.findViewById(R.id.cloth_name)
        private val priceView: TextView = itemView.findViewById(R.id.cloth_price)

        @SuppressLint("SetTextI18n")
        fun bind(cloth: ClothDetailsResponseDTO, context: Context, logItemClick: (ClothDetailsResponseDTO) -> Unit) {
            Glide.with(itemView.context)
                .load(cloth.clothImageURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
            brandView.text = cloth.brand
            nameView.text = cloth.productName
            val price = DecimalFormat("#,###")
            priceView.text = "₩ ${price.format(cloth.price)}"

            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cloth.productURL))
                context.startActivity(intent)
                logItemClick(cloth)
            }
        }
    }
}