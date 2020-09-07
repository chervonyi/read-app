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
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R
import room106.app.read.activities.TitleActivity
import room106.app.read.models.Title
import room106.app.read.views.TitleView

class TitlesListTabFragment: Fragment() {

    // Views
    private lateinit var pullToRefresh: SwipeRefreshLayout
    private lateinit var scrollView: NestedScrollView
    private lateinit var titlesLinearLayout: LinearLayout

    // Firebase
    private lateinit var db: FirebaseFirestore
    var query: Query? = null
    private var initialQuery: Query? = null

    private var isVisibleToUser = false
    private var allTitlesLoaded = false
    private var isLoading = false
    private var listContainsSkeleton = true
    var isSavedTabType = false

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
        db = Firebase.firestore

        allTitlesLoaded = false
        listContainsSkeleton = true

        // Save query for refreshing purpose in future
        initialQuery = query

        loadNextTitles()

        return v
    }

    private fun loadNextTitles() {
        if (query == null) { return }

        // Execute query
        query!!.get()
            .addOnSuccessListener { documents ->
                removeSkeletonScreens()

                if (context != null) {
                    if (documents.size() > 0) {

                        for (document in documents) {
                            val title = document.toObject(Title::class.java)
                            val titleView = TitleView(context!!, title, document.id)

                            titleView.setOnClickListener {
                                val intent = Intent(context, TitleActivity::class.java)

                                if (isSavedTabType) {
                                    val titleID = document["titleID"]
                                    if (titleID is String) {
                                        intent.putExtra("title_id", titleID)
                                    }
                                } else {
                                    intent.putExtra("title_id", document.id)
                                }
                                context?.startActivity(intent)
                            }

                            titlesLinearLayout.addView(titleView)
                        }


                        // Prepare query for next N titles
                        val lastVisibleDocument = documents.documents[documents.size() - 1]
                        query = query!!.startAfter(lastVisibleDocument)
                    } else {
                        allTitlesLoaded = true
                        Log.d("ScrollView", "All titles in 'TitlesListTabFragment' tab have been loaded")
                    }
                }
            }
            .addOnCompleteListener {
                pullToRefresh.isRefreshing = false
                isLoading = false
            }
    }

    private val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        query = initialQuery
        listContainsSkeleton = true
        allTitlesLoaded = false
        loadNextTitles()
    }

    private val onScrollBottomReachListener = ViewTreeObserver.OnScrollChangedListener {
        if (isVisibleToUser) {
            // User scrolling this scrollView

            if (!scrollView.canScrollVertically(1)) {
                // User reach the bottom

                if (!allTitlesLoaded && !isLoading) {
                    // Not all titles has been loaded AND is not loading right now
                    isLoading = true
                    Log.d("ScrollView", "Loading next titles in TitlesListTabFragment")
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

    fun disableUserVisibleFlag() {
        isVisibleToUser = true
    }
}
