package room106.app.read

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import room106.app.read.fragments.*

class CurrentUserFragmentPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> PublishedFragment()
            1 -> PublishedFragment()
            2 -> PublishedFragment()
            3 -> PublishedFragment()
            else -> PublishedFragment()
        }
    }

    override fun getCount(): Int {
        return 4
    }
}