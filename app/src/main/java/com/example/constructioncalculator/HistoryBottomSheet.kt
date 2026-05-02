package com.example.constructioncalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HistoryBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(list: List<String>, title: String): HistoryBottomSheet {
            return HistoryBottomSheet().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putStringArray("records", list.toTypedArray())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx     = requireContext()
        val title   = arguments?.getString("title") ?: "History"
        val records = arguments?.getStringArray("records") ?: arrayOf()

        val scroll = android.widget.ScrollView(ctx)
        val root   = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 48)
        }

        // ── Title ──────────────────────────────────────────────
        root.addView(TextView(ctx).apply {
            text = title
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(0xFF1A2340.toInt())
            setPadding(0, 0, 0, 24)
        })

        // ── Empty state ────────────────────────────────────────
        if (records.isEmpty()) {
            root.addView(TextView(ctx).apply {
                text = "No records found."
                textSize = 14f
                setTextColor(0xFF888888.toInt())
            })
        }

        // ── Records ────────────────────────────────────────────
        records.forEachIndexed { index, record ->
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 20, 24, 20)
                background = ContextCompat.getDrawable(
                    ctx, android.R.drawable.dialog_holo_light_frame
                )
            }

            // رقم السجل
            card.addView(TextView(ctx).apply {
                text = "# ${index + 1}"
                textSize = 11f
                setTextColor(0xFF2563EB.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 0, 0, 8)
            })

            // محتوى السجل (كل سطر منفصل)
            record.split("\n").forEach { line ->
                card.addView(TextView(ctx).apply {
                    text = line
                    textSize = 12f
                    setTextColor(0xFF333333.toInt())
                    setPadding(0, 3, 0, 3)
                })
            }

            root.addView(card, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 14) })
        }

        scroll.addView(root)
        return scroll
    }
}