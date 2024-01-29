package com.example.apptea.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.apptea.DBHelper
import com.example.apptea.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var barChart: BarChart

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize PieChart
        val pieChart: PieChart = binding.pieChart
        initializePieChart(pieChart)

        // Initialize BarChart
        barChart = binding.barChart
        initializeBarChart()

        return root
    }

    private fun initializeBarChart() {
        // Fetch data from the database for the past week
        val dbHelper = DBHelper(requireContext())
        val dailyKilosData = dbHelper.getTeaRecordsForPastWeek()

        // Prepare BarEntries and labels
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val calendar = Calendar.getInstance()

        dailyKilosData.forEachIndexed { index, dailyTeaRecord ->
            entries.add(BarEntry(index.toFloat(), dailyTeaRecord.totalKilos.toFloat()))

            // Format the date to get the day of the week
            val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            val dayOfWeek = dateFormat.format(dailyTeaRecord.date)

            labels.add(dayOfWeek)
        }

        // Create a BarDataSet
        val dataSet = BarDataSet(entries, "Daily Kilos")
        dataSet.color = Color.rgb(76, 175, 80) // Set bars to green
        dataSet.valueTextColor = Color.BLACK

        // Create a BarData object and set it to the BarChart
        val data = BarData(dataSet)
        barChart.data = data

        // Customize chart properties as needed
        barChart.description.text = "Daily Kilos"
        barChart.legend.isEnabled = true

        // Set custom labels for X-axis (days of the week)
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true

        barChart.animateY(2000)

        // Refresh the chart
        barChart.invalidate()
    }





    private fun initializePieChart(pieChart: PieChart) {
        val dbHelper = DBHelper(requireContext())
        val companyKilosData = dbHelper.getCompanyKilosData()

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        companyKilosData.forEach { (company, totalKilos) ->
            entries.add(PieEntry(totalKilos, company))
            colors.add(getRandomColor()) // You can implement a function to generate different colors
        }

        val dataSet = PieDataSet(entries, "Companies")
        dataSet.setColors(colors)

        val data = PieData(dataSet)
        pieChart.data = data

        pieChart.setUsePercentValues(true)
        pieChart.description.text = "Company Kilos"
        pieChart.legend.isEnabled = true
        pieChart.centerText = "Companies"
        pieChart.animateY(2000)

        // Refresh the chart
        pieChart.invalidate()
    }

    private fun getRandomColor(): Int {
        // You can implement a function to generate different colors
        // This is just a simple example
        return Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
