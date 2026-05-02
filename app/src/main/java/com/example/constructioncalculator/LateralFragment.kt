package com.example.constructioncalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.constructioncalculator.databinding.FragmentLateralBinding

class LateralFragment : Fragment() {

    private var _binding: FragmentLateralBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLateralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = DatabaseHelper(requireContext())

        binding.btnCalc.setOnClickListener {
            val phi   = binding.etFriction.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Friction Angle (φ)")
            val delta = binding.etDelta.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Wall Friction Angle (δ)")
            val gamma = binding.etGamma.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Unit Weight (γ)")
            val h     = binding.etHeight.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Please enter Wall Height (H)")

            val r = GeoCalculator.calcLateral(phi, delta, gamma, h)

            binding.tvKa.text = GeoCalculator.fmt(r.ka, 4)
            binding.tvKp.text = GeoCalculator.fmt(r.kp, 4)
            binding.tvK0.text = GeoCalculator.fmt(r.k0, 4)
            binding.tvPa.text = GeoCalculator.fmt(r.pa)
            binding.tvPp.text = GeoCalculator.fmt(r.pp)
            binding.tvP0.text = GeoCalculator.fmt(r.p0)

            binding.layoutResult.visibility = View.VISIBLE

            // Save to database
            db.saveGeoRecord(
                type    = "lateral",
                inputs  = "φ=$phi°, δ=$delta°, γ=$gamma kN/m³, H=$h m",
                results = "Ka=${GeoCalculator.fmt(r.ka, 4)}, Kp=${GeoCalculator.fmt(r.kp, 4)}, " +
                        "K0=${GeoCalculator.fmt(r.k0, 4)}, " +
                        "Pa=${GeoCalculator.fmt(r.pa)} kN/m, " +
                        "Pp=${GeoCalculator.fmt(r.pp)} kN/m, " +
                        "P0=${GeoCalculator.fmt(r.p0)} kN/m"
            )
            val count = db.countGeoByType("lateral")
            Toast.makeText(requireContext(), "✅ Saved — Total records: $count", Toast.LENGTH_SHORT).show()
        }

        binding.btnHistory.setOnClickListener {
            val list = db.getGeoHistory("lateral")
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No saved records", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            HistoryBottomSheet
                .newInstance(list, "🧱 Lateral Pressure History")
                .show(parentFragmentManager, "history_lateral")
        }
    }

    private fun showError(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}