package com.heendoongs.coordibattle.coordi

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
import java.text.DecimalFormat

class DetailClothesAdapter(private val clothes: List<ClothDetailsResponseDTO>, private val context: Context) : RecyclerView.Adapter<DetailClothesAdapter.ClothesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cloth_image, parent, false)
        return ClothesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothesViewHolder, position: Int) {
        val cloth = clothes[position]
        holder.bind(cloth, context)
    }

    override fun getItemCount(): Int = clothes.size

    class ClothesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.cloth_image)
        private val brandView: TextView = itemView.findViewById(R.id.cloth_brand)
        private val nameView: TextView = itemView.findViewById(R.id.cloth_name)
        private val priceView: TextView = itemView.findViewById(R.id.cloth_price)

        @SuppressLint("SetTextI18n")
        fun bind(cloth: ClothDetailsResponseDTO, context: Context) {
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
            }
        }
    }
}