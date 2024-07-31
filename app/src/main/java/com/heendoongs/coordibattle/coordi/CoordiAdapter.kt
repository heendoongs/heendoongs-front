package com.heendoongs.coordibattle.coordi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.databinding.ItemCoordiBinding

/**
 * 코디 어댑터
 * @author 임원정
 * @since 2024.07.30
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.30  	임원정       최초 생성
 * </pre>
 */
class CoordiAdapter(private val context: Context, private var coordiList: MutableList<RankingOrderCoordiListResponseDTO>) :
    RecyclerView.Adapter<CoordiAdapter.CoordiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoordiViewHolder {
        val binding = ItemCoordiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoordiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoordiViewHolder, position: Int) {
        val item = coordiList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return coordiList.size
    }

    fun updateData(newData: List<RankingOrderCoordiListResponseDTO>) {
        coordiList.clear()
        coordiList.addAll(newData)
        notifyDataSetChanged()
    }

    fun appendData(newData: List<RankingOrderCoordiListResponseDTO>) {
        coordiList.addAll(newData)
        notifyDataSetChanged()
    }

    inner class CoordiViewHolder(private val binding: ItemCoordiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RankingOrderCoordiListResponseDTO) {
            binding.tvCoordiTitle.text = item.coordiTitle
            binding.tvNickname.text = "by. ${item.nickname}"

            // Base64 이미지를 Bitmap으로 변환
            val bitmap = decodeBase64ToBitmap(item.coordiImage)
            if (bitmap != null) {
                binding.ivCoordiImage.setImageBitmap(bitmap)
            }
        }
    }

    private fun decodeBase64ToBitmap(base64Image: String?): Bitmap? {
        return base64Image?.let {
            val decodedString = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }
}