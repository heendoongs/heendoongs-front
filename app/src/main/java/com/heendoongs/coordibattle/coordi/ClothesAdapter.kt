import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.heendoongs.coordibattle.R

class ClothesAdapter(private val onItemClicked: (Int) -> Unit) : RecyclerView.Adapter<ClothesAdapter.ClothesViewHolder>() {

    private val clothesList = listOf(
        R.drawable.face1,
        R.drawable.face2,
        R.drawable.face3,
        // 추가 이미지 리소스
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClothesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clothes, parent, false)
        return ClothesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClothesViewHolder, position: Int) {
        holder.bind(clothesList[position])
    }

    override fun getItemCount() = clothesList.size

    inner class ClothesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageResId: Int) {
            imageView.setImageResource(imageResId)
            itemView.setOnClickListener {
                onItemClicked(imageResId)
            }
        }
    }
}
