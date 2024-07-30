package com.heendoongs.coordibattle.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.heendoongs.coordibattle.R

class OutLineTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeColor: Int
    private var strokeWidthVal: Float

    init {
        //attributes 가져오기
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OutLineTextView)
        strokeWidthVal = typedArray.getFloat(R.styleable.OutLineTextView_textStrokeWidth, 3f)
        strokeColor = typedArray.getColor(R.styleable.OutLineTextView_textStrokeColor, Color.WHITE)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val originalTextColor: ColorStateList = textColors // 기본 텍스트 색상 저장
            val text = text.toString()

            // draw stroke
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidthVal
            setTextColor(strokeColor)
            super.onDraw(canvas)

            // draw fill
            paint.style = Paint.Style.FILL
            setTextColor(originalTextColor)
            super.onDraw(canvas)
        }
    }
}