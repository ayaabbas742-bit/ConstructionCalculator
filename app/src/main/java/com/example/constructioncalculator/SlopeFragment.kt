package com.example.constructioncalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.constructioncalculator.databinding.FragmentSlopeBinding

class SlopeFragment : Fragment() {

    private var _binding: FragmentSlopeBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlopeBinding.inflate(inflater, container, false)
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
            val h     = binding.etHeight.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Slope Height (H)")
            val beta  = binding.etBeta.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Slope Angle (β)")

            val r = GeoCalculator.calcSlope(c, phi, gamma, h, beta)

            binding.tvFS.text     = GeoCalculator.fmt(r.fs)
            binding.tvNs.text     = GeoCalculator.fmt(r.ns)
            binding.tvHc.text     = GeoCalculator.fmt(r.hc)
            binding.tvStatus.text = r.status

            binding.layoutResult.visibility = View.VISIBLE

            // Save to database
            db.saveGeoRecord(
                type    = "slope",
                inputs  = "c=$c kPa, φ=$phi°, γ=$gamma kN/m³, H=$h m, β=$beta°",
                results = "FS=${GeoCalculator.fmt(r.fs)}, Ns=${GeoCalculator.fmt(r.ns)}, " +
                        "Hc=${GeoCalculator.fmt(r.hc)} m, Status=${r.status}"
            )
            val count = db.countGeoByType("slope")
            Toast.makeText(requireContext(), "✅ Saved — Total records: $count", Toast.LENGTH_SHORT).show()
        }

        binding.btnHistory.setOnClickListener {
            val list = db.getGeoHistory("slope")
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No saved records", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            HistoryBottomSheet
                .newInstance(list, "⛰ Slope Stability History")
                .show(parentFragmentManager, "history_slope")
        }
    }

    private fun showError(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}