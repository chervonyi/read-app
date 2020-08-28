package room106.app.read

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.fragments.*

class CurrentUserFragmentPageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser ?: return TitlesListTabFragment()

        return when(position) {
            0 -> {
                val publishedFragment = TitlesListTabFragment()
                publishedFragment.query = db.collection("titles")
                    .whereEqualTo("authorID", currentUser.uid)
                    .whereEqualTo("status", "published")
                    .orderBy("publicationTime", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)
                publishedFragment
            }
            1 -> {
                val draftFragment = TitlesListTabFragment()
                draftFragment.query = db.collection("titles")
                    .whereEqualTo("authorID", currentUser.uid)
                    .whereEqualTo("status", "draft")
                    .orderBy("publicationTime", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)
                draftFragment
            }
            2 -> {
                val likedFragment = TitlesListTabFragment()
                likedFragment.query = db.collection("liked")
                    .whereEqualTo("userID", currentUser.uid)
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)
                likedFragment.isSavedTabType = true
                likedFragment
            }
            3 -> {
                val savedFragment = TitlesListTabFragment()
                savedFragment.query = db.collection("saved")
                    .whereEqualTo("userID", currentUser.uid)
                    .orderBy("time", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)
                savedFragment.isSavedTabType = true
                savedFragment
            }
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