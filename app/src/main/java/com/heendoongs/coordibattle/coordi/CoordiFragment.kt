package com.heendoongs.coordibattle.coordi

import ClothesAdapter
import android.R.attr.*
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.RetrofitConnection
import com.heendoongs.coordibattle.databinding.FragmentCoordiBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
//import com.flask.colorpicker.ColorPickerDialog
//import com.flask.colorpicker.OnColorSelectedListener
//import com.github.dhaval2404.colorpicker.ColorPickerDialog
import java.nio.file.attribute.AclEntry.newBuilder


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
    private var _binding: FragmentCoordiBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val GALLERY_REQUEST_CODE = 1
    }

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

        // 배경 선택 버튼 클릭 리스너
        binding.btnSelectBackground.setOnClickListener {
            toggleBackgroundSelectionLayout()
        }

        // 색상 선택 버튼 클릭 리스너
        binding.btnSelectColor.setOnClickListener {
//            showColorPicker()
        }

        // 이미지 선택 버튼 클릭 리스너
        binding.btnSelectImage.setOnClickListener {
            openGalleryForImage()
        }

        return view
    }

    /**
     * 리사이클러뷰 초기화
     */
    private fun setupRecyclerView() {
        heendyAdapter = HeendyAdapter(getFaceItems()) { imageResId ->
            changeFaceImage(imageResId)
        }
        clothesAdapter = ClothesAdapter(requireContext(), emptyList()) { clothesId, imageUrl ->
            addRemoteImageToContainer(clothesId, imageUrl)
        }
        binding.itemList.layoutManager = GridLayoutManager(context, 3) // 한 행에 3개 아이템 표시
        binding.itemList.adapter = heendyAdapter // 기본 흰디 얼굴 선택
    }

    /**
     * 아이템 탭별 동작 매칭
     */
    private fun setupItemTabs() {
        binding.faceIcon.setOnClickListener {
            selectTab(binding.faceIcon)
            loadLocalItems(getFaceItems())
        }
        binding.armsIcon.setOnClickListener {
            selectTab(binding.armsIcon)
            loadLocalItems(getArmsItems())
        }
        binding.topIcon.setOnClickListener {
            selectTab(binding.topIcon)
            loadRemoteItems("Top")
        }
        binding.bottomIcon.setOnClickListener {
            selectTab(binding.bottomIcon)
            loadRemoteItems("Bottom")
        }
        binding.shoesIcon.setOnClickListener {
            selectTab(binding.shoesIcon)
            loadRemoteItems("Shoe")
        }
        selectTab(binding.faceIcon)
    }

    /**
     * 선택된 탭
     */
    private fun selectTab(selectedIcon: ImageView) {
        val icons = listOf(binding.faceIcon, binding.armsIcon, binding.topIcon, binding.bottomIcon, binding.shoesIcon)
        icons.forEach { it.isSelected = false }
        selectedIcon.isSelected = true
    }

    /**
     * 얼굴 아이템 가져오기
     */
    private fun getFaceItems(): List<Int> {
        return listOf(R.drawable.img_face1, R.drawable.img_face2, R.drawable.img_face3, R.drawable.img_face4, R.drawable.img_face5, R.drawable.img_face6)
    }

    /**
     * 팔 아이템 가져오기
     */
    private fun getArmsItems(): List<Int> {
        return listOf(R.drawable.img_left_arm1, R.drawable.img_left_arm2, R.drawable.img_right_arm1, R.drawable.img_right_arm2)
    }

    /**
     * 안드로이드에 저장된 아이템(얼굴, 팔) 로드
     */
    private fun loadLocalItems(items: List<Int>) {
        heendyAdapter = HeendyAdapter(items) { imageResId ->
            if (items == getFaceItems()) {
                changeFaceImage(imageResId)
            } else if (items == getArmsItems()) {
                changeArmImage(imageResId)
            }
        }
        binding.itemList.adapter = heendyAdapter
        heendyAdapter.notifyDataSetChanged()
    }

    /**
     * 얼굴 변경
     */
    private fun changeFaceImage(imageResId: Int) {
        binding.face.setImageResource(imageResId)
    }

    /**
     * 팔 변경
     */
    private fun changeArmImage(imageResId: Int) {
        when (imageResId) {
            // 왼쪽팔
            R.drawable.img_left_arm1, R.drawable.img_left_arm2 -> {
                binding.leftArm.setImageResource(imageResId)
                setupImageView(binding.leftArm, 320, 340, 0,130)
                if (binding.leftArm.parent == null) {
                    binding.coordiContainer.addView(binding.leftArm)
                }
            }
            // 오른쪽 팔
            R.drawable.img_right_arm1, R.drawable.img_right_arm2 -> {
                binding.rightArm.setImageResource(imageResId)
                setupImageView(binding.rightArm,320, 340, 130, 0)
                if (binding.rightArm.parent == null) {
                    binding.coordiContainer.addView(binding.rightArm)
                }
            }
        }
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
     * 코디 영역에 DB에 저장된 이미지 올리기
     */
    private fun addRemoteImageToContainer(clothId: Long, imageUrl: String) {
        if (!selectedClothIds.contains(clothId)) {
            selectedClothIds.add(clothId)
        }
        val imageView = ImageView(requireContext())
        Glide.with(this).load(imageUrl).into(imageView)
        setupImageView(imageView, 300, 300)
        binding.coordiContainer.addView(imageView)
    }

    /**
     * 이미지뷰 설정
     */
    private fun setupImageView(imageView: ImageView,
                               width: Int = FrameLayout.LayoutParams.WRAP_CONTENT, height: Int = FrameLayout.LayoutParams.WRAP_CONTENT,
                               marginStart: Int = 0, marginEnd: Int = 0) {
        imageView.layoutParams = FrameLayout.LayoutParams(
            width,
            height
        ).apply {
            setMarginStart(marginStart)
            setMarginEnd(marginEnd)
            gravity = Gravity.CENTER
        }

        /**
         * 이미지 뷰 터치 설정
         */
        imageView.setOnTouchListener(object : View.OnTouchListener {
            private val scaleDetector = ScaleGestureDetector(requireContext(), ScaleListener(imageView))
            private val rotateDetector = RotateGestureDetector()
            private var initialX = 0f
            private var initialY = 0f
            private var dX = 0f
            private var dY = 0f

            /**
             * 드래그 앤 드랍
             */
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                scaleDetector.onTouchEvent(event)
                rotateDetector.onTouch(event)

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
            private inner class RotateGestureDetector : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                private var previousAngle = 0f

                fun onTouch(event: MotionEvent): Boolean {
                    if (event.pointerCount == 2) {
                        val angle = getRotationAngle(event)
                        if (previousAngle != 0f) {
                            val deltaAngle = angle - previousAngle
                            imageView.rotation += deltaAngle
                        }
                        previousAngle = angle
                    } else {
                        previousAngle = 0f
                    }
                    return true
                }

                private fun getRotationAngle(event: MotionEvent): Float {
                    val dx = (event.getX(0) - event.getX(1)).toDouble()
                    val dy = (event.getY(0) - event.getY(1)).toDouble()
                    return Math.toDegrees(Math.atan2(dy, dx)).toFloat()
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
    }

    private fun toggleBackgroundSelectionLayout() {
        if (binding.backgroundSelectionLayout.visibility == View.GONE) {
            binding.backgroundSelectionLayout.visibility = View.VISIBLE
        } else {
            binding.backgroundSelectionLayout.visibility = View.GONE
        }
    }

//    private fun showColorPicker() {
//        val colorPicker = ColorPickerDialogBuilder
//            .with(context)
//            .setTitle("색상 선택")
//            .initialColor(Color.WHITE)
//            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
//            .density(12)
//            .setPositiveButton("확인") { dialog, selectedColor, allColors ->
//                binding.coordiContainer.setBackgroundColor(selectedColor)
//            }
//            .setNegativeButton("취소") { dialog, which -> }
//            .build()
//        colorPicker.show()
//    }
//    private fun showColorPicker() {
//    ColorPickerDialog.Builder(context)
//        .setDialogType(ColorPickerDialog.TYPE_PRESETS)
//        .setAllowCustom(true)
//        .setShowAlphaSlider(true)
//        .setDialogId(0)
//        .setPresets(arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
//        .setOnColorSelectedListener(OnColorSelectedListener { selectedColor ->
//            binding.coordiContainer.setBackgroundColor(selectedColor)
//        })
//        .show(parentFragmentManager, "color_dialog")
//    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                setBackgroundImage(uri)
            }
        }
    }

    private fun setBackgroundImage(imageUri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            binding.coordiContainer.background = BitmapDrawable(resources, bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
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