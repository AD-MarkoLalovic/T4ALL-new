package com.mobility.enp.view.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mobility.enp.interf.VpInterface
import com.mobility.enp.view.fragments.intro.IntroScreenAbout
import com.mobility.enp.view.fragments.intro.IntroScreenAdvantages
import com.mobility.enp.view.fragments.intro.IntroScreenRegions

class ViewPager2Adapter(
    fragmentActivity: FragmentActivity,
    private val vpInterface: VpInterface
) : FragmentStateAdapter(fragmentActivity) {

    private val fragments: List<Fragment> by lazy {
        listOf(
            IntroScreenAbout().apply { setInterface(vpInterface) },
            IntroScreenRegions().apply { setInterface(vpInterface) },
            IntroScreenAdvantages().apply { setInterface(vpInterface) }
        )
    }

    override fun createFragment(position: Int): Fragment {
        return fragments.getOrElse(position) { fragments[0] }
    }

    override fun getItemCount(): Int {
        return NUM_PAGES
    }

    companion object {
        private const val NUM_PAGES = 3 // Number of fragments
    }
}
