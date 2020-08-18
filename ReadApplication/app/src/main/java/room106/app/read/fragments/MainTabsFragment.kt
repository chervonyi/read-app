package room106.app.read.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import room106.app.read.R
import room106.app.read.TitleTypesFragmentPageAdapter

class MainTabsFragment: Fragment() {

    // Views
    private lateinit var titleTypesTabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_main_tabs, container, false)

        // Connect views
        titleTypesTabLayout = v.findViewById(R.id.titleTypesTabLayout)
        viewPager = v.findViewById(R.id.viewPager)

        // Prepare tab
        titleTypesTabLayout.addOnTabSelectedListener(onTabSelectedListener)
        viewPager.adapter = TitleTypesFragmentPageAdapter(childFragmentManager)
        viewPager.addOnPageChangeListener(onPageChangeListener)

        return v
    }

    //region Tab Listeners
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(p0: TabLayout.Tab?) { }

        override fun onTabUnselected(p0: TabLayout.Tab?) { }

        override fun onTabSelected(p0: TabLayout.Tab?) {
            viewPager.currentItem = p0!!.position
        }
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) { }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) { }

        override fun onPageSelected(position: Int) {
            val tab = titleTypesTabLayout.getTabAt(position)
            tab?.select()
        }
    }
    //endregion
}