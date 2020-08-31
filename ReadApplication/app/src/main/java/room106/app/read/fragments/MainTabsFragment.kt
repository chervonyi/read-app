package room106.app.read.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import room106.app.read.R
import room106.app.read.adapters.Title3TypesFragmentPageAdapter
import room106.app.read.adapters.Title4TypesFragmentPageAdapter

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

        // Style tabLayout
        if (context != null) {
            titleTypesTabLayout.setTabTextColors(
                ContextCompat.getColor(context!!, R.color.colorTab),
                ContextCompat.getColor(context!!, R.color.colorTabSupporting))
        }

        // Prepare tabs
        titleTypesTabLayout.addTab(titleTypesTabLayout.newTab().setText(getString(R.string.tab_new)))
        titleTypesTabLayout.addTab(titleTypesTabLayout.newTab().setText(getString(R.string.tab_recommended)))
        titleTypesTabLayout.addTab(titleTypesTabLayout.newTab().setText(getString(R.string.tab_top)))
//        titleTypesTabLayout.addTab(titleTypesTabLayout.newTab().setText(getString(R.string.tab_followed)))

        titleTypesTabLayout.addOnTabSelectedListener(onTabSelectedListener)
        viewPager.adapter = Title3TypesFragmentPageAdapter(childFragmentManager)
        viewPager.addOnPageChangeListener(onPageChangeListener)
        viewPager.offscreenPageLimit = 3

        return v
    }

    private var _isUserLoggedIn = true
    var isUserLoggedIn: Boolean
        get() = _isUserLoggedIn
        set(value) {
            _isUserLoggedIn = value

            if (value && titleTypesTabLayout.tabCount == 3)  {
                titleTypesTabLayout.addTab(titleTypesTabLayout.newTab().setText(getString(R.string.tab_followed)))
                viewPager.adapter = Title4TypesFragmentPageAdapter(childFragmentManager)
            } else if (!value && titleTypesTabLayout.tabCount == 4){
                titleTypesTabLayout.removeTabAt(3)
                viewPager.adapter = Title3TypesFragmentPageAdapter(childFragmentManager)
            }
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