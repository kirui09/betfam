package com.example.apptea.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apptea.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        // Initialize BarChart
        val barChart: BarChart = binding.barChart
        initializeBarChart(barChart)

        // Initialize PieChart
        val pieChart: PieChart = binding.pieChart
        initializePieChart(pieChart)

        return root
    }

    private fun initializeBarChart(barChart: BarChart) {
        // Sample data for demonstration
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1f, 10f))
        entries.add(BarEntry(2f, 20f))
        entries.add(BarEntry(3f, 15f))
        entries.add(BarEntry(4f, 25f))
        entries.add(BarEntry(5f, 5f))
        entries.add(BarEntry(6f, 20f))
        entries.add(BarEntry(7f, 14f))

        val dataSet = BarDataSet(entries, "Kilos Per Day")


        dataSet.setColors(ColorTemplate.COLORFUL_COLORS,255)
       dataSet.valueTextColor=Color.BLACK
        val data = BarData(dataSet)
        barChart.setFitBars(true)
        barChart.data = data


        // Customize chart properties as needed
        barChart.description.text= "Daily Progress"
        barChart.animateY(2000)
        barChart.setDrawBarShadow(false)
        barChart.setDrawValueAboveBar(true)

        // Disable gridlines for the Y-axis (vertical gridlines)
        val leftYAxis = barChart.axisLeft
        leftYAxis.setDrawGridLines(false)

        val rightYAxis = barChart.axisRight
        rightYAxis.setDrawGridLines(false)
        // Disable gridlines for the X-axis (horizontal gridlines)
        val xAxis = barChart.xAxis
        xAxis.setDrawGridLines(false)

        // Refresh the chart
        barChart.invalidate()

    }
    private fun initializePieChart(pieChart: PieChart) {
        // Sample data for demonstration
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(30f, "Kaisugu"))
        entries.add(PieEntry(50f, "KTDA"))
        entries.add(PieEntry(10f, "SASINI"))
        entries.add(PieEntry(10f, "SIRET"))



        val dataSet = PieDataSet(entries, "Companies")

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS,255)

        dataSet.valueTextSize=15f

        dataSet.valueTextColor=Color.BLACK

        val data = PieData(dataSet)

        pieChart.data = data

        // Customize chart properties as needed
        pieChart.setUsePercentValues(true)
        pieChart.description.text = "Kampuni"
        pieChart.legend.isEnabled = true
        pieChart.centerText="Kampuni"
        pieChart.animateY(2000)

        // Refresh the chart
        pieChart.invalidate()
    }
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

    }

