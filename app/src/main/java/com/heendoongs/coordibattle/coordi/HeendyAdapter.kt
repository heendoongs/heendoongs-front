package com.heendoongs.coordibattle.coordi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.databinding.ItemClothBinding

/**
 * 흰디(얼굴, 팔) 어댑터
 * @author 임원정
 * @since 2024.08.02
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.02  	임원정       최초 생성
 * </pre>
 */

class HeendyAdapter(private val items: List<Int>, private val itemClick: (Int) -> Unit) :
    RecyclerView.Adapter<HeendyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClothBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemClothBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageResId: Int) {
            binding.imageView.setImageResource(imageResId)
            itemView.setOnClickListener {
                itemClick(imageResId)
            }
        }
    }
}
