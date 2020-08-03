package room106.app.read

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import room106.app.read.fragments.FollowedFragment
import room106.app.read.fragments.NewFragment
import room106.app.read.fragments.RecommendedFragment
import room106.app.read.fragments.TopFragment

class TitleTypesFragmentPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> NewFragment()
            1 -> RecommendedFragment()
            2 -> TopFragment()
            3 -> FollowedFragment()
            else -> NewFragment()
        }
    }

    override fun getCount(): Int {
        return 4
    }
}