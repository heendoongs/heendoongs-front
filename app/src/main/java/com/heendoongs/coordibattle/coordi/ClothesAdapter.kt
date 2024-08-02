import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.coordi.ClothesResponseDTO
import com.heendoongs.coordibattle.databinding.ItemClothBinding

/**
 * 옷 리스트 어댑터
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.28  	임원정       최초 생성
 * </pre>
 */

class ClothesAdapter(
    private val context: Context,
    private var clothes: List<ClothesResponseDTO>,
    private val itemClick: (Long, String) -> Unit
) : RecyclerView.Adapter<ClothesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClothBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(clothes[position])
    }

    override fun getItemCount(): Int = clothes.size

    fun updateData(newClothes: List<ClothesResponseDTO>) {
        clothes = newClothes
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemClothBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cloth: ClothesResponseDTO) {
            Log.d("ClothesAdapter", "Loading image URL: ${cloth.clothImageURL}")
            Glide.with(context).load(cloth.clothImageURL).into(binding.imageView)
            itemView.setOnClickListener {
                itemClick(cloth.clothId, cloth.clothImageURL)
            }
        }
    }
}