package room106.app.read.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R
import room106.app.read.activities.TitleActivity
import room106.app.read.models.LikedTitle
import room106.app.read.models.Title
import room106.app.read.views.TitleView

class SavedFragment: Fragment() {

    // Views
    private lateinit var scrollView: NestedScrollView
    private lateinit var titlesLinearLayout: LinearLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var nextTitlesQuery: Query? = null

    private var isVisibleToUser = false
    private var allTitlesLoaded = false
    private var isLoading = false
    private var listContainsSkeleton = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_titles_list, container, false)

        // Connect Views
        scrollView = v.findViewById(R.id.scrollView)
        titlesLinearLayout = v.findViewById(R.id.titlesLinearLayout)

        // Attach listeners
        scrollView.viewTreeObserver.addOnScrollChangedListener(onScrollBottomReachListener)

        // Firebase
        db = Firebase.firestore
        auth = Firebase.auth

        allTitlesLoaded = false
        listContainsSkeleton = true

        // TODO - Remove delay
        Handler().postDelayed({
            loadNextTitles()
        }, 3000)

        return v
    }

    private fun loadNextTitles() {
        val currentUserID = auth.currentUser?.uid ?: return

        if (nextTitlesQuery == null) {
            nextTitlesQuery = db.collection("saved")
                .whereEqualTo("userID", currentUserID)
                .orderBy("time", Query.Direction.DESCENDING)
                .limit(NewFragment.TITLES_LIMIT)
        }

        // Execute query
        nextTitlesQuery!!.get()
            .addOnSuccessListener { documents ->
                removeSkeletonScreens()

                if (documents.size() > 0) {
                    for (document in documents) {
                        val likedTitle = document.toObject(LikedTitle::class.java)

                        val titleView = TitleView(context, likedTitle)
                        titleView.setOnClickListener {
                            val intent = Intent(context, TitleActivity::class.java)
                            intent.putExtra("title_id", likedTitle.titleID)
                            context?.startActivity(intent)
                        }

                        titlesLinearLayout.addView(titleView)
                    }

                    // Prepare query for next N titles
                    val lastVisibleDocument = documents.documents[documents.size() - 1]
                    nextTitlesQuery = db.collection("saved")
                        .whereEqualTo("userID", currentUserID)
                        .orderBy("time", Query.Direction.DESCENDING)
                        .startAfter(lastVisibleDocument)
                        .limit(NewFragment.TITLES_LIMIT)
                } else {
                    allTitlesLoaded = true
                    Log.d("ScrollView", "All titles in 'SAVED' tab have been loaded")
                }
            }
            .addOnCompleteListener {
                isLoading = false
            }
    }

    private val onScrollBottomReachListener = ViewTreeObserver.OnScrollChangedListener {
        if (isVisibleToUser) {
            // User scrolling this scrollView

            if (!scrollView.canScrollVertically(1)) {
                // User reach the bottom

                if (!allTitlesLoaded && !isLoading) {
                    // Not all titles has been loaded AND is not loading right now
                    isLoading = true
                    Log.d("ScrollView", "Loading next titles in SavedFragment")
                    loadNextTitles()
                }
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        this.isVisibleToUser = isVisibleToUser
    }

    private fun removeSkeletonScreens() {
        if (listContainsSkeleton) {
            titlesLinearLayout.removeAllViews()
            listContainsSkeleton = false
        }
    }
}
