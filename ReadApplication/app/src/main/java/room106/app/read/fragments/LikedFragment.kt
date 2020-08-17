package room106.app.read.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import room106.app.read.models.LikedTitle
import room106.app.read.models.Title
import room106.app.read.views.TitleView

class LikedFragment: Fragment() {

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
        db = Firebase.firestore
        auth = Firebase.auth

        loadTitles()
        return v
    }

    private fun loadTitles() {
        titlesLinearLayout.removeAllViews()
        val currentUserID = auth.currentUser?.uid ?: return

        val likedTitlesRef = db.collection("liked")
            .whereEqualTo("userID", currentUserID)
            .orderBy("time", Query.Direction.DESCENDING)

        // Execute query
        likedTitlesRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val likedTitle = document.toObject(LikedTitle::class.java)

                    val titleView = TitleView(context, likedTitle)
                    titleView.setOnClickListener {
                        val intent = Intent(context, TitleActivity::class.java)
                        intent.putExtra("title_id", likedTitle.titleID)
                        context?.startActivity(intent)
//                        activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                    }

                    titlesLinearLayout.addView(titleView)
                }
            }
    }
}
