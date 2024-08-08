package com.heendoongs.coordibattle.coordi.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.coordi.dto.CoordiListResponseDTO
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
 * 2024.07.31   임원정       이미지 처리 오류 수정, 디자인 수정
 * 2024.08.02   임원정       onItemClick 메소드 및 인터페이스 추가
 * </pre>
 */
class CoordiAdapter(private val context: Context,
                    private var coordiList: MutableList<CoordiListResponseDTO>,
                    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<CoordiAdapter.CoordiViewHolder>() {

    // 아이템 터치 감지 인터페이스
    interface OnItemClickListener {
        fun onItemClick(item: CoordiListResponseDTO)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoordiViewHolder {
        val binding = ItemCoordiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoordiViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: CoordiViewHolder, position: Int) {
        val item = coordiList[position]
        holder.bind(item)
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(item)
        }
    }

    /**
     * 아이템 개수 반환
     */
    override fun getItemCount(): Int {
        return coordiList.size
    }

    /**
     * 데이터 변경 됐을 때
     */
    fun updateData(newData: List<CoordiListResponseDTO>) {
        coordiList.clear()
        coordiList.addAll(newData)
        notifyDataSetChanged()
    }

    /**
     * 데이터 추가 됐을 떄
     */
    fun appendData(newData: List<CoordiListResponseDTO>) {
        coordiList.addAll(newData)
        notifyDataSetChanged()
    }

    inner class CoordiViewHolder(private val binding: ItemCoordiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CoordiListResponseDTO) {
            binding.tvCoordiTitle.text = item.coordiTitle
            binding.tvNickname.text = "by. ${item.nickname}"

            // 이미지 로딩 함수 호출
            loadImage(item.coordiImage, binding.ivCoordiImage)
        }

        // 이미지 가져오기
        private fun loadImage(base64Image: String, imageView: ImageView) {
            val bitmap = decodeBase64ToBitmap(base64Image)
            bitmap?.let {
                Glide.with(context).load(it).into(imageView)
            }
        }
        
        // Base64 To Bitmap 변환
        private fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
            return try {
                val base64Data = base64Str.substringAfter(",")
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                null
            }
        }
    }
}