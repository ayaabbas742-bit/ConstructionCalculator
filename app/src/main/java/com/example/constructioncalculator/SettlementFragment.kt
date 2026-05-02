package com.example.constructioncalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.constructioncalculator.databinding.FragmentSettlementBinding

class SettlementFragment : Fragment() {

    private var _binding: FragmentSettlementBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettlementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())

        binding.btnCalc.setOnClickListener {

            val q = binding.etPressure.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter pressure (q)")

            val b = binding.etWidth.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter width (B)")

            val es = binding.etEs.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter Es")

            val v = binding.etPoisson.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter Poisson ratio")

            val h = binding.etH.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter height (H)")

            val cc = binding.etCc.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter Cc")

            val cs = binding.etCs.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter Cs")

            val e0 = binding.etE0.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter e0")

            val sigma0 = binding.etSigma0.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter σ0")

            val sigmaC = binding.etSigmaC.text.toString().toDoubleOrNull()
                ?: return@setOnClickListener showError("Enter σc")

            val result = GeoCalculator.calcSettlement(
                q, b, es, v, h, cc, cs, e0, sigma0, sigmaC
            )

            binding.tvOCStatus.text =
                if (result.isOC) "Over-Consolidated (OC)"
                else "Normally Consolidated (NC)"

            binding.tvSi.text = "${GeoCalculator.fmt(result.si)} mm"
            binding.tvSc.text = "${GeoCalculator.fmt(result.sc)} mm"
            binding.tvSt.text = "${GeoCalculator.fmt(result.st)} mm"

            binding.layoutResult.visibility = View.VISIBLE

            db.saveGeoRecord(
                type = "settlement",
                inputs = "q=$q, B=$b, Es=$es, v=$v, H=$h, Cc=$cc, Cs=$cs, e0=$e0, σ0=$sigma0, σc=$sigmaC",
                results = "Si=${result.si}, Sc=${result.sc}, St=${result.st}"
            )

            Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
        }

        binding.btnHistory.setOnClickListener {
            val list = db.getGeoHistory("settlement")

            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No history", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            HistoryBottomSheet
                .newInstance(list, "Settlement History")
                .show(parentFragmentManager, "history")
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}