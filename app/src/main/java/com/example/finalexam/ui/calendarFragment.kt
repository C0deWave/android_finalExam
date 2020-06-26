package com.example.finalexam.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finalexam.R
import kotlinx.android.synthetic.main.fragment_calendar.*


class calendarFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_day.visibility = View.INVISIBLE
        tv_content.visibility = View.INVISIBLE
        cardView.visibility = View.INVISIBLE
        btn_enter.visibility = View.INVISIBLE

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            tv_day.text = String.format("%d.%d.%d", year, month + 1, dayOfMonth)
            tv_day.visibility = View.VISIBLE

            btn_enter.visibility = View.VISIBLE

            checkDay(tv_day.text.toString())
        }
        btn_enter.setOnClickListener {
            cardView.visibility = View.VISIBLE

        }
        btn_save.setOnClickListener {
            cardView.visibility = View.INVISIBLE
            tv_content.visibility = View.VISIBLE

            when(rg.checkedRadioButtonId) {
                R.id.rbtn_complete -> tv_content.text = "공부완료"
                R.id.rbtn_uncomplete -> tv_content.text = "공부 미완료"
            }
        }
    }

    fun checkDay(cDay: String){

    }

    companion object{
        fun newInstance(): calendarFragment {
            return calendarFragment()
        }
    }

}