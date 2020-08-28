package room106.app.read.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import room106.app.read.adapters.CurrentUserFragmentPageAdapter
import room106.app.read.R

class CurrentUserFragment: Fragment() {

    // Views
    private lateinit var viewPager: ViewPager
    private lateinit var currentUserTabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_current_user_titles, container, false)

        viewPager = v.findViewById(R.id.viewPager)
        currentUserTabLayout = v.findViewById(R.id.currentUserTabLayout)

        // Connect Listeners
        currentUserTabLayout.addOnTabSelectedListener(onTabSelectedListener)
        viewPager.adapter = CurrentUserFragmentPageAdapter(childFragmentManager)
        viewPager.addOnPageChangeListener(onPageChangeListener)
        viewPager.offscreenPageLimit = 3

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
            val tab = currentUserTabLayout.getTabAt(position)
            tab?.select()
        }
    }
    //endregion
}