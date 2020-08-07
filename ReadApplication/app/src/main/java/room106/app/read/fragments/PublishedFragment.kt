package room106.app.read.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R
import room106.app.read.activities.TitleActivity
import room106.app.read.models.Title
import room106.app.read.views.TitleView

class PublishedFragment: Fragment() {

    // Views
    private lateinit var titlesLinearLayout: LinearLayout

    // Firebase
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

        return v
    }

    override fun onStart() {
        super.onStart()
        loadTitles()
    }

    private fun loadTitles() {
        // TODO - CHANGE LOADING ALL TITLE FOR USER'S PUBLISHED TITLES!
        val titlesRef = db.collection("titles")

        // Execute query
        titlesRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val title = document.toObject(Title::class.java)
                val titleView = TitleView(context, title, document.id)

                titleView.setOnClickListener {
                    val intent = Intent(context, TitleActivity::class.java)
                    intent.putExtra("title_id", document.id)
                    context?.startActivity(intent)
                    activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                }

                titlesLinearLayout.addView(titleView)
            }
        }
    }
}
