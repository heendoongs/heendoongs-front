import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heendoongs.coordibattle.R
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

class ClothesAdapter(private val itemClick: (Int) -> Unit) : RecyclerView.Adapter<ClothesAdapter.ViewHolder>() {

    private val clothes = listOf(R.drawable.face1, R.drawable.face2, R.drawable.face3)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClothBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(clothes[position])
    }

    override fun getItemCount(): Int = clothes.size

    inner class ViewHolder(private val binding: ItemClothBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageResId: Int) {
            binding.imageView.setImageResource(imageResId)
            itemView.setOnClickListener {
                itemClick(imageResId)
            }
        }
    }
}
