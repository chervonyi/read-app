package room106.app.read.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.fragments.*

class Title4TypesFragmentPageAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val db = Firebase.firestore

        return when(position) {
            0 -> {
                val newFragment = TitlesListTabFragment()
                newFragment.query = db.collection("titles")
                    .whereEqualTo("status", "published")
                    .orderBy("publicationTime", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)

                newFragment
            }
            1 -> {
                val recommendedFragment = TitlesListTabFragment()
                recommendedFragment.query = db.collection("titles")
                    .whereEqualTo("status", "published")
                    .orderBy("likesCount", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)
                recommendedFragment
            }
            2 -> {
                val topFragment = TitlesListTabFragment()
                topFragment.query = db.collection("titles")
                    .whereEqualTo("status", "published")
                    .orderBy("readsCount", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)
                topFragment
            }
            3 -> FollowedFragment()
            else -> TitlesListTabFragment()
        }
    }

    override fun getCount(): Int {
        return 4
    }

    companion object {
        const val TITLES_LIMIT: Long = 3
    }
}