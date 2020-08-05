package room106.app.read.fragments

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
import room106.app.read.models.Title
import room106.app.read.views.TitleView

class NewFragment: Fragment() {

    // Views
    private lateinit var titlesLinearLayout: LinearLayout

    // Firebase
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_new, container, false)

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
        // TODO - Add some limitation like: whereEqualTo.. order.. limits..
        val titlesRef = db.collection("titles")

        // Execute query
        titlesRef.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val title = document.toObject(Title::class.java)
                val titleView = TitleView(context, title, document.id)
                titlesLinearLayout.addView(titleView)
            }
        }
    }
}
