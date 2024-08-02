package com.heendoongs.coordibattle.coordi

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import java.text.DecimalFormat

class DetailClothesAdapter(private val clothes: List<ClothDetailsResponseDTO>) : RecyclerView.Adapter<DetailClothesAdapter.ClothesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cloth_image, parent, false)
        return ClothesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothesViewHolder, position: Int) {
        val cloth = clothes[position]
        holder.bind(cloth)
    }

    override fun getItemCount(): Int = clothes.size

    class ClothesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.cloth_image)
        private val brandView: TextView = itemView.findViewById(R.id.cloth_brand)
        private val nameView: TextView = itemView.findViewById(R.id.cloth_name)
        private val priceView: TextView = itemView.findViewById(R.id.cloth_price)

        @SuppressLint("SetTextI18n")
        fun bind(cloth: ClothDetailsResponseDTO) {
            Glide.with(itemView.context)
                .load(cloth.clothImageURL)
                .into(imageView)
            brandView.text = cloth.brand
            nameView.text = cloth.productName
            val price = DecimalFormat("#,###")
            priceView.text = "₩ ${price.format(cloth.price)}"
        }

        interface OnItemClickListener{
            fun onItemClick(url:String)
        }

        var itemClickListener:OnItemClickListener?=null

//        inner class ViewHolder(val binding: FavoriteRowBinding):RecyclerView.ViewHolder(binding.root){
//            init {
//                binding.root.setOnClickListener {
//                    itemClickListener?.onItemClick(items[adapterPosition].url)
//                }
//            }
//        }
    }
}