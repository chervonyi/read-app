package room106.app.read.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
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
    private lateinit var titlesLinearLayout: LinearLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_titles_list, container, false)

        // Connect Views
        titlesLinearLayout = v.findViewById(R.id.titlesLinearLayout)

        // Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        loadTitles()
        return v
    }

    private fun loadTitles() {
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

                getFollowedAuthorsTitles(usersID)
            }
    }

    private fun getFollowedAuthorsTitles(usersID: List<String>) {
        titlesLinearLayout.removeAllViews()

        if (usersID.isNotEmpty()) {
            val titlesRef = db.collection("titles")
                .whereEqualTo("status", "published")
                .whereIn("authorID", usersID)
                .orderBy("publicationTime", Query.Direction.DESCENDING)

            // Execute query
            titlesRef.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.toObject(Title::class.java)
                    val titleView = TitleView(context, title, document.id)

                    titleView.setOnClickListener {
                        val intent = Intent(context, TitleActivity::class.java)
                        intent.putExtra("title_id", document.id)
                        context?.startActivity(intent)
//                        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                    }

                    titlesLinearLayout.addView(titleView)
                }
            }
        }
    }
}
