package com.netzwelt.tutormodule.ui.dashboard.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autohub.skln.fragment.BaseFragment
import com.netzwelt.tutormodule.R
import com.netzwelt.tutormodule.databinding.FragmentTutorScheduleBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Vt Netzwelt
 */

class ScheduleFragment : BaseFragment() {

    private lateinit var mBinding: FragmentTutorScheduleBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_tutor_schedule, container, false)

    companion object {
        fun newInstance(): ScheduleFragment = ScheduleFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = FragmentTutorScheduleBinding.bind(view)
        val dates = getDates("01-01-" + Calendar.getInstance().get(Calendar.YEAR),
                "31-12-" + Calendar.getInstance().get(Calendar.YEAR))

        initializeCalendarView(dates)
        mBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i < 10) {
                    mBinding.calendarView.scrollToPosition(0)
                } else if (i > 90) {
                    mBinding.calendarView.scrollToPosition(dates.size - 1)
                } else if ((i * 3) < (dates.size - 1)) {
                    mBinding.calendarView.scrollToPosition(i * 3)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }

    private fun initializeCalendarView(dates: List<String>) {
        mBinding.calendarView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val adapter = ScheduleAdapter(dates)
        mBinding.calendarView.adapter = adapter
    }


    private fun getDates(dateString1: String, dateString2: String): List<String> {
        val dates = ArrayList<String>()
        val df1 = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

        val format = SimpleDateFormat("MMM,dd,EEE", Locale.ENGLISH)

        lateinit var date1: Date
        lateinit var date2: Date

        try {
            date1 = df1.parse(dateString1)!!
            date2 = df1.parse(dateString2)!!
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val cal1 = Calendar.getInstance()
        cal1.time = date1


        val cal2 = Calendar.getInstance()
        cal2.time = date2

        while (!cal1.after(cal2)) {
            dates.add(format.format(cal1.time))
            cal1.add(Calendar.DATE, 1)
        }
        return dates
    }}