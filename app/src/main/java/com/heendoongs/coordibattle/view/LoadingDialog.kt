package com.heendoongs.coordibattle.view

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.heendoongs.coordibattle.R

class ProgressDialog(context: Context) : Dialog(context) {
    //커스텀 다이얼로그
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE) // 다이얼로그 제목 안 보이게
        setContentView(R.layout.dialog_loading)
    }
}