package com.example.constructioncalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.constructioncalculator.databinding.FragmentSoilBinding

class SoilFragment : Fragment() {

    private var _binding: FragmentSoilBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSoilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())

        binding.btnClassify.setOnClickListener {
            val ll       = binding.etLL.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Liquid Limit")
            val pl       = binding.etPL.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Plastic Limit")
            val moisture = binding.etMoisture.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Moisture Content")
            val fine     = binding.etFine.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Fine Percentage")

            val r = GeoCalculator.classifySoil(ll, pl, moisture, fine)

            binding.tvSymbol.text      = r.symbol
            binding.tvGroup.text       = r.group
            binding.tvDescription.text = r.description
            binding.tvPI.text          = GeoCalculator.fmt(r.pi) + " %"
            binding.tvLI.text          = GeoCalculator.fmt(r.li)
            binding.tvCI.text          = GeoCalculator.fmt(r.ci)

            binding.layoutResult.visibility = View.VISIBLE

            // Save to database
            db.saveGeoRecord(
                type    = "soil",
                inputs  = "LL=$ll, PL=$pl, Moisture=$moisture, Fine=$fine%",
                results = "Symbol=${r.symbol}, Group=${r.group}, PI=${GeoCalculator.fmt(r.pi)}%, " +
                        "LI=${GeoCalculator.fmt(r.li)}, CI=${GeoCalculator.fmt(r.ci)}"
            )
            val count = db.countGeoByType("soil")
            Toast.makeText(requireContext(), "✅ Saved — Total records: $count", Toast.LENGTH_SHORT).show()
        }

        binding.btnHistory.setOnClickListener {
            val list = db.getGeoHistory("soil")
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No saved records", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            HistoryBottomSheet
                .newInstance(list, "🪨 Soil Classification History")
                .show(parentFragmentManager, "history_soil")
        }
    }

    private fun showError(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}