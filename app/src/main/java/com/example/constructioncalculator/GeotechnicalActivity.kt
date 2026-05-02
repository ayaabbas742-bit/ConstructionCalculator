package com.example.constructioncalculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.constructioncalculator.databinding.ActivityGeotechnicalBinding
import com.google.android.material.tabs.TabLayoutMediator

class GeotechnicalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGeotechnicalBinding

    private val tabs = listOf(
        "🪨 Soil",
        "🏗 Bearing",
        "📉 Settlement",
        "⛰ Slope",
        "🧱 Lateral Pressure"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeotechnicalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.adapter = GeoPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 4

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position]
        }.attach()

        binding.btnBack.setOnClickListener { finish() }
    }

    inner class GeoPagerAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {

        override fun getItemCount() = tabs.size

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> SoilFragment()
            1 -> BearingFragment()
            2 -> SettlementFragment()
            3 -> SlopeFragment()
            4 -> LateralFragment()
            else -> SoilFragment()
        }
    }
}