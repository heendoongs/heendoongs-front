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
    private lateinit var saveButton: Button
    private lateinit var uploadButton: Button
    private lateinit var imageContainer: FrameLayout
    private lateinit var clothesList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_coordi, container, false)

        saveButton = view.findViewById(R.id.btn_save)
        uploadButton = view.findViewById(R.id.btn_upload)
        imageContainer = view.findViewById(R.id.coordi_container)
        clothesList = view.findViewById(R.id.clothes_list)

        saveButton.setOnClickListener {
            saveImageToGallery()
        }

        uploadButton.setOnClickListener {
            // 업로드 로직 추가
        }

        setupClothesList()

        return view
    }

    private fun setupClothesList() {
        clothesList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        clothesList.adapter = ClothesAdapter { imageResId ->
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

        imageView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(requireContext(), GestureListener(imageView))
            private val scaleDetector = ScaleGestureDetector(requireContext(), ScaleListener(imageView))
            private var rotationDegrees = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                scaleDetector.onTouchEvent(event)

                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        v.x = event.rawX - v.width / 2
                        v.y = event.rawY - v.height / 2
                    }
                }
                return true
            }

            private inner class GestureListener(private val imageView: ImageView) : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(_ : MotionEvent?): Boolean {
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

        imageContainer.addView(imageView)
    }

    private fun saveImageToGallery() {
        val bitmap = Bitmap.createBitmap(imageContainer.width, imageContainer.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageContainer.draw(canvas)

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
}