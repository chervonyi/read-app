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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R
import room106.app.read.activities.TitleActivity
import room106.app.read.models.Title
import room106.app.read.views.TitleView

class FollowedFragment: Fragment() {

    // Views
    private lateinit var pullToRefresh: SwipeRefreshLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var titlesLinearLayout: LinearLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var nextTitlesQuery: Query? = null

    private var isVisibleToUser = false
    private var allTitlesLoaded = false
    private var isLoading = false

    private var followedUsersID = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_titles_list, container, false)

        // Connect Views
        pullToRefresh = v.findViewById(R.id.pullToRefresh)
        scrollView = v.findViewById(R.id.scrollView)
        titlesLinearLayout = v.findViewById(R.id.titlesLinearLayout)

        // Attach listeners
        scrollView.viewTreeObserver.addOnScrollChangedListener(onScrollBottomReachListener)
        pullToRefresh.setOnRefreshListener(onRefreshListener)

        // Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        return v
    }

    override fun onStart() {
        super.onStart()
        skeletonIsShown = true
        allTitlesLoaded = false
        nextTitlesQuery = null
        loadFollowedUsersIDs()
    }

    private fun loadFollowedUsersIDs() {
        val currentUserID = auth.currentUser?.uid ?: return

        // Get usersID that current user is following
        db.collection("following")
            .whereEqualTo("followerID", currentUserID)
            .get().addOnSuccessListener { documents ->
                val usersID = ArrayList<String>()

                for (document in documents) {
                    val id = document["userID"]
                    if (id is String) {
                        usersID.add(id)
                    }
                }

                followedUsersID = usersID
                loadNextTitles()
            }
    }

    private fun loadNextTitles() {
        if (followedUsersID.isNotEmpty()) {

            var initialLoad = false

            if (nextTitlesQuery == null) {
                initialLoad = true
                nextTitlesQuery = db.collection("titles")
                    .whereEqualTo("status", "published")
                    .whereIn("authorID", followedUsersID)
                    .orderBy("publicationTime", Query.Direction.DESCENDING)
                    .limit(TITLES_LIMIT)
            }

            // Execute query
            nextTitlesQuery!!.get()
                .addOnSuccessListener { documents ->

                    if (initialLoad) {
                        skeletonIsShown = false
                        initialLoad = false
                    }

                    if (documents.size() > 0) {
                        for (document in documents) {
                            val title = document.toObject(Title::class.java)
                            val titleView = TitleView(context, title, document.id)

                            titleView.setOnClickListener {
                                val intent = Intent(context, TitleActivity::class.java)
                                intent.putExtra("title_id", document.id)
                                context?.startActivity(intent)
                            }

                            titlesLinearLayout.addView(titleView)
                        }

                        // Prepare query for next N titles
                        val lastVisibleDocument = documents.documents[documents.size() - 1]
                        nextTitlesQuery = db.collection("titles")
                            .whereEqualTo("status", "published")
                            .whereIn("authorID", followedUsersID)
                            .orderBy("publicationTime", Query.Direction.DESCENDING)
                            .startAfter(lastVisibleDocument)
                            .limit(TITLES_LIMIT)
                    } else {
                        allTitlesLoaded = true
                        Log.d("ScrollView", "All titles in 'Followed' tab have been loaded")
                    }
                }
                .addOnCompleteListener {
                    isLoading = false
                }
        } else {
            skeletonIsShown = false
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
                    Log.d("ScrollView", "Loading next titles in FollowedFragment")
                    loadNextTitles()
                }
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        this.isVisibleToUser = isVisibleToUser
    }

    private var _skeletonIsShown = true
    var skeletonIsShown: Boolean
        get() {
            return _skeletonIsShown
        }
        set(value) {
            _skeletonIsShown = value
            titlesLinearLayout.removeAllViews()

            if (_skeletonIsShown) {
                // Add 3 skeleton titles
                for (i in 1..3) {
                    val skeletonView = LayoutInflater.from(context)
                        .inflate(R.layout.title_skeleton_layout, titlesLinearLayout, false)
                    titlesLinearLayout.addView(skeletonView)
                }
            }
        }

    private val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        pullToRefresh.isRefreshing = false
    }

    companion object {
        const val TITLES_LIMIT: Long = 3
    }
}
