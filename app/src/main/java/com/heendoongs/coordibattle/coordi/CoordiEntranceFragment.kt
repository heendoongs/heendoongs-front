package com.heendoongs.coordibattle.coordi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.heendoongs.coordibattle.MainActivity
import com.heendoongs.coordibattle.R
import com.heendoongs.coordibattle.battle.BattleFragment
import com.heendoongs.coordibattle.coordi.HomeFragment
import com.heendoongs.coordibattle.global.checkLoginAndNavigate

class CoordiEntranceFragment : Fragment()  {

    private lateinit var rootView: View
    private lateinit var helpBox: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_coordi_entrance, container, false)

        if (!checkLoginAndNavigate()) {
            return rootView
        }

        helpBox = rootView.findViewById(R.id.coordi_entrance_help_text_box)
        helpBox.isVisible = false
        helpBox.isClickable = false
        loadCoordiEntrance()
        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (!checkLoginAndNavigate()) {
            Toast.makeText(requireContext(), "로그인 후 이용 가능합니다.", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun  loadCoordiEntrance() {
        val startBtn = rootView.findViewById<Button>(R.id.coordi_entrance_start_btn)
        val helpBtn = rootView.findViewById<Button>(R.id.coordi_entrance_help_btn)
        val homeBtn = rootView.findViewById<Button>(R.id.coordi_entrance_home_btn)

        startBtn.setOnClickListener {
            val fragment = CoordiFragment()
            val mainActivity = context as MainActivity
            mainActivity.replaceFragment(fragment, R.id.fragment_coordi)
        }

        helpBtn.setOnClickListener {
            helpBox.isVisible = true
            helpBox.isClickable = true
            val helpCloseBtn = rootView.findViewById<ImageButton>(R.id.coordi_entrance_help_close_btn)
            helpCloseBtn.setOnClickListener {
                helpBox.isVisible = false
                helpBox.isClickable = false
            }
        }

        homeBtn.setOnClickListener {
            val fragment = HomeFragment()
            val mainActivity = context as MainActivity
            mainActivity.replaceFragment(fragment, R.id.fragment_home)
        }
    }
}
