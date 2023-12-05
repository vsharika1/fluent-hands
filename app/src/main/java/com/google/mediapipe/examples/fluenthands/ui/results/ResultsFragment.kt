package com.google.mediapipe.examples.fluenthands.ui.results

import android.os.Bundle
import android.security.identity.ResultData
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.mediapipe.examples.fluenthands.databinding.FragmentResultsBinding
import com.google.mediapipe.examples.fluenthands.db.Result
import com.google.mediapipe.examples.fluenthands.db.ResultDatabase
import com.google.mediapipe.examples.fluenthands.db.ResultDatabaseDao
import com.google.mediapipe.examples.fluenthands.db.ResultRepository
import com.google.mediapipe.examples.fluenthands.db.ResultViewModel
import com.google.mediapipe.examples.fluenthands.db.ResultViewModelFactory

class ResultsFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var adapter: ResultsAdapter
    private lateinit var database: ResultDatabase
    private lateinit var databaseDao: ResultDatabaseDao
    private lateinit var repository: ResultRepository
    private lateinit var viewModelFactory: ResultViewModelFactory
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var deleteButton: Button
    private val resultDataList = mutableListOf<ResultData>()
    private lateinit var scoreLineChart: LineChart
    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = ResultsFragment()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        val view = binding.root

        setupViewModel()
        setupListView()
        setupLineChart()

        return view
    }

    private fun setupViewModel() {
        val application = requireNotNull(this.activity).application
        database = ResultDatabase.getInstance(application)
        databaseDao = database.resultDatabaseDao
        repository = ResultRepository(databaseDao)
        viewModelFactory = ResultViewModelFactory(repository)
        resultViewModel = ViewModelProvider(this, viewModelFactory).get(ResultViewModel::class.java)

        resultViewModel.allResultData.observe(viewLifecycleOwner) { results ->
            adapter.replace(results)
            adapter.notifyDataSetChanged()

            if (results.isNotEmpty()) {
                populateScoreLineChart(results)
            } else {
                binding.scoreLineChart.clear() // Clear chart if no data
            }
        }
    }

    private fun setupListView() {
        adapter = ResultsAdapter(requireContext(), emptyList())
        binding.resultsList.adapter = adapter
    }

//    private fun setupLineChart() {
//        binding.scoreLineChart.apply {
//            description.isEnabled = false
//            setTouchEnabled(true)
//            isDragEnabled = true
//            setScaleEnabled(true)
//            setPinchZoom(true)
//        }
//    }
private fun setupLineChart() {
    binding.scoreLineChart.description.isEnabled = false
    binding.scoreLineChart.setTouchEnabled(true)
    binding.scoreLineChart.isDragEnabled = true
    binding.scoreLineChart.setScaleEnabled(true)
    binding.scoreLineChart.setPinchZoom(true)

    // Set Y-Axis range
    binding.scoreLineChart.axisLeft.axisMaximum = 100f // Maximum score
    binding.scoreLineChart.axisLeft.axisMinimum = 0f // Minimum score
    binding.scoreLineChart.axisRight.isEnabled = false // Disable the right Y-Axis

    // Set X-Axis to represent quiz numbers
    binding.scoreLineChart.xAxis.granularity = 1f // Show only integer values
    binding.scoreLineChart.xAxis.setDrawGridLines(false) // Disable grid lines if desired
}

    private fun populateScoreLineChart(results: List<Result>) {
        val entries = results.mapIndexed { index, resultData ->
            Entry(index.toFloat(), resultData.score.toFloat())
        }

        val dataSet = LineDataSet(entries, "Scores").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toMutableList()
        }

        binding.scoreLineChart.data = LineData(dataSet)
        binding.scoreLineChart.invalidate() // Refresh the chart
    }


    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}