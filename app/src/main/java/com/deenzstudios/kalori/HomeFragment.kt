package com.deenzstudios.kalori

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.common.AdRequest
import android.widget.FrameLayout



class HomeFragment : Fragment() {

    private lateinit var viewPager: ViewPager2

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewPager = view.findViewById(R.id.viewPager)

        val bannerContainer = view.findViewById<FrameLayout>(R.id.bannerContainer)

        val bannerAd = BannerAdView(requireContext())

        bannerAd.setAdUnitId("R-M-19366932-2")

        bannerAd.setAdSize(
            BannerAdSize.stickySize(requireContext(), 320)
        )

        bannerContainer.addView(bannerAd)

        bannerAd.loadAd(
            AdRequest.Builder().build()
        )

        val images = listOf(
            R.drawable.poster1,
            R.drawable.poster2
        )

        val adapter = SliderAdapter(images)
        viewPager.adapter = adapter

        autoSlide()

        return view
    }

    private fun autoSlide() {

        val runnable = object : Runnable {

            override fun run() {

                val nextItem = (viewPager.currentItem + 1) % 2

                viewPager.currentItem = nextItem

                handler.postDelayed(this, 3000)
            }
        }

        handler.postDelayed(runnable, 3000)
    }
}