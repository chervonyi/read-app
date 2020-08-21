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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R
import room106.app.read.activities.TitleActivity
import room106.app.read.models.Title
import room106.app.read.views.TitleView

class TopFragment: Fragment() {

    // Views
    private lateinit var scrollView: NestedScrollView
    private lateinit var titlesLinearLayout: LinearLayout

    // Firebase
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

        allTitlesLoaded = false
        listContainsSkeleton = true

        // TODO - Remove delay
        Handler().postDelayed({
            loadNextTitles()
        }, 5000)

        return v
    }

    private fun loadNextTitles() {
        if (nextTitlesQuery == null) {
            nextTitlesQuery = db.collection("titles")
                .whereEqualTo("status", "published")
                .orderBy("readsCount", Query.Direction.DESCENDING)
                .limit(TITLES_LIMIT)
        }

        // Execute query
        nextTitlesQuery!!.get()
            .addOnSuccessListener { documents ->
                if (documents.size() > 0) {

                    // Remove skeleton views
                    if (listContainsSkeleton) {
                        titlesLinearLayout.removeAllViews()
                        listContainsSkeleton = false
                    }

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
                        .orderBy("readsCount", Query.Direction.DESCENDING)
                        .startAfter(lastVisibleDocument)
                        .limit(TITLES_LIMIT)
                } else {
                    allTitlesLoaded = true
                    Log.d("ScrollView", "All titles in 'Top' tab have been loaded")
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
                    Log.d("ScrollView", "Loading next titles in TopFragment")
                    loadNextTitles()
                }
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        this.isVisibleToUser = isVisibleToUser
    }

    companion object {
        const val TITLES_LIMIT: Long = 3
    }
}
