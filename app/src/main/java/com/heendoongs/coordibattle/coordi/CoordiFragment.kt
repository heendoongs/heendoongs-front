package com.heendoongs.coordibattle.coordi

import ClothesAdapter
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.databinding.FragmentCoordiBinding
import java.io.IOException

/**
 * 옷입히기 프래그먼트
 * @author 임원정
 * @since 2024.07.26
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.26  	임원정       최초 생성
 * </pre>
 */

class CoordiFragment : Fragment() {
    private var _binding: FragmentCoordiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCoordiBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnSave.setOnClickListener {
            saveImageToGallery()
        }

        binding.btnUpload.setOnClickListener {
            // 업로드 로직 추가
        }

        setupClothesList()

        return view
    }

    private fun setupClothesList() {
        binding.itemList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.itemList.adapter = ClothesAdapter { imageResId ->
            addImageToContainer(imageResId)
        }
    }

    private fun addImageToContainer(imageResId: Int) {
        val imageView = ImageView(requireContext())
        imageView.setImageResource(imageResId)
        imageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        var rotationDegrees = 0f

        imageView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(requireContext(), GestureListener(imageView))
            private val scaleDetector = ScaleGestureDetector(requireContext(), ScaleListener(imageView))
            private var initialX = 0f
            private var initialY = 0f
            private var dX = 0f
            private var dY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                scaleDetector.onTouchEvent(event)

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 터치가 시작될 때의 뷰의 위치와 터치 포인트를 기록
                        initialX = v.x
                        initialY = v.y
                        dX = event.rawX - initialX
                        dY = event.rawY - initialY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // 터치 포인트가 이동할 때마다 뷰를 새로운 위치로 이동
                        v.x = event.rawX - dX
                        v.y = event.rawY - dY
                    }
                }
                return true
            }
            private inner class GestureListener(private val imageView: ImageView) : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(event: MotionEvent?): Boolean {
                    rotationDegrees += 90f
                    imageView.rotation = rotationDegrees
                    return true
                }
            }

            private inner class ScaleListener(private val imageView: ImageView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    imageView.scaleX *= detector.scaleFactor
                    imageView.scaleY *= detector.scaleFactor

                    return true
                }
            }
        })

        binding.coordiContainer.addView(imageView)
    }

    /**
     * 갤러리에 이미지 저장
     */
    private fun saveImageToGallery() {
        val bitmap = Bitmap.createBitmap(binding.coordiContainer.width, binding.coordiContainer.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        binding.coordiContainer.draw(canvas)

        val resolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "DressUp_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                    throw IOException("Failed to save bitmap.")
                }
            }
            Toast.makeText(requireContext(), "이미지 저장 성공!", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(requireContext(), "이미지 저장 실패!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}