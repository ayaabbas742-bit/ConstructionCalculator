package com.example.constructioncalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.constructioncalculator.databinding.FragmentBearingBinding

class BearingFragment : Fragment() {

    private var _binding: FragmentBearingBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBearingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())

        binding.btnCalc.setOnClickListener {
            val c     = binding.etCohesion.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Cohesion (c)")
            val phi   = binding.etFriction.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Friction Angle (φ)")
            val gamma = binding.etGamma.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Unit Weight (γ)")
            val df    = binding.etDepth.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Foundation Depth (Df)")
            val b     = binding.etWidth.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Foundation Width (B)")
            val l     = binding.etLength.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Foundation Length (L)")
            val fs    = binding.etFS.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Safety Factor (FS)")

            val r = GeoCalculator.calcBearing(c, phi, gamma, df, b, l, fs)

            binding.tvNc.text   = GeoCalculator.fmt(r.nc)
            binding.tvNq.text   = GeoCalculator.fmt(r.nq)
            binding.tvNy.text   = GeoCalculator.fmt(r.ny)
            binding.tvQu.text   = GeoCalculator.fmt(r.qu)
            binding.tvQnet.text = GeoCalculator.fmt(r.qnet)
            binding.tvQa.text   = GeoCalculator.fmt(r.qa)

            binding.layoutResult.visibility = View.VISIBLE

            // Save to DatabaseHelper
            db.saveGeoRecord(
                type    = "bearing",
                inputs  = "c=$c kPa, φ=$phi°, γ=$gamma kN/m³, Df=$df m, B=$b m, L=$l m, FS=$fs",
                results = "Nc=${GeoCalculator.fmt(r.nc)}, Nq=${GeoCalculator.fmt(r.nq)}, " +
                        "Nγ=${GeoCalculator.fmt(r.ny)}, qu=${GeoCalculator.fmt(r.qu)} kPa, " +
                        "qnet=${GeoCalculator.fmt(r.qnet)} kPa, qa=${GeoCalculator.fmt(r.qa)} kPa"
            )
            val count = db.countGeoByType("bearing")
            Toast.makeText(requireContext(), "✅ Saved — Total records: $count", Toast.LENGTH_SHORT).show()
        }

        binding.btnHistory.setOnClickListener {
            val list = db.getGeoHistory("bearing")
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No saved records", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            HistoryBottomSheet
                .newInstance(list, "🏗 Bearing Capacity History")
                .show(parentFragmentManager, "history_bearing")
        }
    }

    private fun showError(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}