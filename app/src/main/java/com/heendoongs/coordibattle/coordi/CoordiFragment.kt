package com.heendoongs.coordibattle.coordi

import ClothesAdapter
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.RetrofitConnection
import com.heendoongs.coordibattle.databinding.FragmentCoordiBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * 옷입히기 프래그먼트
 * @author 임원정
 * @since 2024.07.29
 * @version 1.0
 *
 * <pre>
 * 수정일        	수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.07.29  	임원정       최초 생성
 * 2024.07.29   임원정       이미지 드래그앤드롭, 확대/축소, 이미지 저장 구현
 * 2024.08.02   임원정       아이템 탭 메뉴 동작 및 아이템 불러오기 구현
 * 2024.08.03   임원정       코디 업로드 기능 구현
 * </pre>
 */

class CoordiFragment : Fragment() {
    private var _binding: com.heendoongs.coordibattle.databinding.FragmentCoordiBinding? = null
    private val binding get() = _binding!!

    private lateinit var heendyAdapter: HeendyAdapter
    private lateinit var clothesAdapter: ClothesAdapter
    private lateinit var service: CoordiService

    private var selectedClothIds = mutableListOf<Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCoordiBinding.inflate(inflater, container, false)
        val view = binding.root

        service = RetrofitConnection.getInstance().create(CoordiService::class.java)

        setupItemTabs()
        setupRecyclerView()

        binding.btnSave.setOnClickListener {
            saveImageToGallery()
        }

        binding.btnUpload.setOnClickListener {
            showUploadDialog()
        }

        // 기본으로 얼굴 아이콘이 선택된 상태로 설정
        selectTab(binding.faceIcon)

        return view
    }

    /**
     * 리사이클러뷰 초기화
     */
    private fun setupRecyclerView() {
        heendyAdapter = HeendyAdapter(getFaceItems()) { imageResId ->
            addLocalImageToContainer(imageResId)
        }
        clothesAdapter = ClothesAdapter(requireContext(), emptyList()) { clothesId, imageUrl ->
            addRemoteImageToContainer(clothesId, imageUrl)
        }
        binding.itemList.layoutManager = GridLayoutManager(context, 3) // 한 행에 3개 아이템 표시
        binding.itemList.adapter = heendyAdapter // Default adapter
    }

    /**
     * 아이템 탭별 동작 매칭
     */
    private fun setupItemTabs() {
        binding.faceIcon.setOnClickListener { selectTab(binding.faceIcon); loadLocalItems(getFaceItems()) }
        binding.armsIcon.setOnClickListener { selectTab(binding.armsIcon); loadLocalItems(getArmsItems()) }
        binding.topIcon.setOnClickListener { selectTab(binding.topIcon); loadRemoteItems("Top") }
        binding.bottomIcon.setOnClickListener { selectTab(binding.bottomIcon); loadRemoteItems("Bottom") }
        binding.shoesIcon.setOnClickListener { selectTab(binding.shoesIcon); loadRemoteItems("Shoe") }
    }

    private fun selectTab(selectedTab: ImageView) {
        val tabs = listOf(binding.faceIcon, binding.armsIcon, binding.topIcon, binding.bottomIcon, binding.shoesIcon)

        for (tab in tabs) {
            tab.isSelected = tab == selectedTab
        }
    }

    /**
     * 얼굴 가져오기
     */
    private fun getFaceItems(): List<Int> {
        return listOf(R.drawable.img_face1, R.drawable.img_face2, R.drawable.img_face3, R.drawable.img_face4, R.drawable.img_face5, R.drawable.img_face6)
    }

    /**
     * 팔 가져오기
     */
    private fun getArmsItems(): List<Int> {
        return listOf(R.drawable.img_arm1, R.drawable.img_arm2)
    }

    /**
     * 로컬로 저장된 아이템(얼굴, 팔) 가져오기
     */
    private fun loadLocalItems(items: List<Int>) {
        heendyAdapter = HeendyAdapter(items) { imageResId ->
            addLocalImageToContainer(imageResId)
        }
        binding.itemList.adapter = heendyAdapter
        heendyAdapter.notifyDataSetChanged() // Refresh the local adapter
    }

    /**
     * DB에 저장된 아이템(상의, 하의, 신발) 가져오기
     */
    private fun loadRemoteItems(type: String) {
        service.getClothesList(type).enqueue(object : Callback<List<ClothesResponseDTO>> {
            override fun onResponse(call: Call<List<ClothesResponseDTO>>, response: Response<List<ClothesResponseDTO>>) {
                if (response.isSuccessful && response.body() != null) {
                    clothesAdapter.updateData(response.body()!!)
                    binding.itemList.adapter = clothesAdapter
                }
            }

            override fun onFailure(call: Call<List<ClothesResponseDTO>>, t: Throwable) {
                Log.e("CoordiFragment", "Error loading clothes", t)
            }
        })
    }

    /**
     * 코디 영역에 로컬 이미지 올리기
     */
    private fun addLocalImageToContainer(imageResId: Int) {
        val imageView = ImageView(requireContext())
        imageView.setImageResource(imageResId)
        setupImageView(imageView)
    }

    /**
     * 코디 영역에 DB에 저장된 이미지 올리기
     */
    private fun addRemoteImageToContainer(clothId: Long, imageUrl: String) {
        if (!selectedClothIds.contains(clothId)) {
            selectedClothIds.add(clothId)
        }
        val imageView = ImageView(requireContext())
        Glide.with(this).load(imageUrl).into(imageView)
        setupImageView(imageView)
    }

    /**
     * 이미지뷰 설정
     */
    private fun setupImageView(imageView: ImageView) {
        imageView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        var rotationDegrees = 0f

        /**
         * 이미지 뷰 터치 설정
         */
        imageView.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector = GestureDetector(requireContext(), GestureListener(imageView))
            private val scaleDetector = ScaleGestureDetector(requireContext(), ScaleListener(imageView))
            private var initialX = 0f
            private var initialY = 0f
            private var dX = 0f
            private var dY = 0f

            /**
             * 드래그 앤 드랍
             */
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                scaleDetector.onTouchEvent(event)

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = v.x
                        initialY = v.y
                        dX = event.rawX - initialX
                        dY = event.rawY - initialY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        v.x = event.rawX - dX
                        v.y = event.rawY - dY
                    }
                }
                return true
            }

            /**
             * 회전
             */
            private inner class GestureListener(private val imageView: ImageView) : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(event: MotionEvent?): Boolean {
                    rotationDegrees += 90f
                    imageView.rotation = rotationDegrees
                    return true
                }
            }

            /**
             *  확대/축소
             */
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
     * 업로드 시 제목 입력 Dialog
     */
    private fun showUploadDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_upload, null)
        val editTextTitle = dialogView.findViewById<EditText>(R.id.editTextTitle)

        AlertDialog.Builder(requireContext())
            .setTitle("제목 입력")
            .setView(dialogView)
            .setPositiveButton("확인") { dialog, which ->
                val title = editTextTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    uploadCoordi(title)
                } else {
                    Toast.makeText(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /**
     * 코디 업로드
     */
    private fun uploadCoordi(title: String) {
        val bitmap = Bitmap.createBitmap(binding.coordiContainer.width, binding.coordiContainer.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        binding.coordiContainer.draw(canvas)

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

        val request = CoordiCreateRequestDTO(
            memberId = 6002L, // 실제 데이터로 변경 예정
            title = title,
            coordiImage = encodedImage,
            clothIds = selectedClothIds
        )

        service.uploadCoordi(request).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "업로드 성공!", Toast.LENGTH_SHORT).show()
                    navigateToHomeFragment()
                } else {
                    Toast.makeText(requireContext(), "업로드 실패!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(requireContext(), "업로드 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToHomeFragment() {
        val homeFragment = HomeFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.main_container, homeFragment)
            .addToBackStack(null)
            .commit()
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