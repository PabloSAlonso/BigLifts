package com.biglifts.workouttracker.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.biglifts.workouttracker.databinding.FragmentVolumeByMuscleBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VolumeByMuscleFragment : Fragment() {

    private var _binding: FragmentVolumeByMuscleBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VolumeByMuscleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVolumeByMuscleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        observeData()
    }

    private fun setupChart() {
        binding.chart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            legend.isEnabled = true
            xAxis.setDrawGridLines(false)
            axisLeft.granularity = 1f
            axisRight.isEnabled = false
        }
    }

    private fun observeData() {
        viewModel.volumeData.observe(viewLifecycleOwner) { data ->
            if (data.isEmpty()) return@observe

            val entries = data.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.volume.toFloat())
            }

            val dataSet = BarDataSet(entries, "Volume (sets × reps × weight)").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 10f
            }

            binding.chart.data = BarData(dataSet)
            binding.chart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(
                data.map { it.muscle }
            )
            binding.chart.invalidate()

            // Update summary
            binding.tvTotalVolume.text = "${data.sumOf { it.volume }.toInt()} total"
            binding.tvTopMuscle.text = "Top: ${data.maxByOrNull { it.volume }?.muscle ?: "--"}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}