package room106.app.read.activities

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.models.Title


class TitleActivity : AppCompatActivity() {

    // Views
    private lateinit var mainNestedScrollView: NestedScrollView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var headerTextView: TextView
    private lateinit var authorAvatarImageView: RoundedImageView
    private lateinit var authorTextView: TextView
    private lateinit var readsCountTextView: TextView
    private lateinit var likesCountTextView: TextView
    private lateinit var timeToReadTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var bodyTextView: TextView

    // Firebase
    private lateinit var db: FirebaseFirestore

    private var titleID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        // Connect views
        mainNestedScrollView = findViewById(R.id.mainNestedScrollView)
        appBarLayout = findViewById(R.id.appBarLayout)
        headerTextView = findViewById(R.id.titleHeaderTextView)
        authorAvatarImageView = findViewById(R.id.titleAuthorAvatarImageView)
        authorTextView = findViewById(R.id.titleAuthorTextView)
        readsCountTextView = findViewById(R.id.titleReadsCountTextView)
        likesCountTextView = findViewById(R.id.titleLikesCountTextView)
        timeToReadTextView = findViewById(R.id.titleTimeToReadTextView)
        descriptionTextView = findViewById(R.id.titleDescriptionTextView)
        bodyTextView = findViewById(R.id.titleBodyTextView)

        if (intent.hasExtra("title_id")) {
            titleID = intent.getStringExtra("title_id")
        }

        db = Firebase.firestore
    }

    override fun onStart() {
        super.onStart()
        loadTitle()
    }

    private fun loadTitle() {
       if (titleID != null) {
           val titleRef = db.collection("titles").document(titleID!!)

           titleRef.get().addOnSuccessListener { document ->
               val title = document.toObject(Title::class.java)

               document.reference.collection("body").document("text")
                   .get().addOnSuccessListener { bodyDocument ->
                       val body = bodyDocument.get("text").toString()
                       updateTitleUI(title, body)
                   }
           }
       }
    }

    private fun updateTitleUI(title: Title?, body: String) {
        if (title != null) {
            headerTextView.text = title.title
            authorTextView.text = title.authorName
            descriptionTextView.text = title.description
            readsCountTextView.text = title.readsCount.toString()
            likesCountTextView.text = title.likesCount.toString()

            // TODO - timeToReadTextView.text = body.length / 100 ...
            bodyTextView.text = body

            val avatarName = "ic_avatar_${title.authorAvatar}"
            val image = resources.getIdentifier(avatarName, "drawable", packageName)
            authorAvatarImageView.setImageResource(image)
        }
    }

    //region Listeners
    fun onClickBack(v: View) {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    fun onClickScrollTop(v: View) {
        mainNestedScrollView.fullScroll(ScrollView.FOCUS_UP)
        appBarLayout.setExpanded(true)
    }

    fun onClickMore(v: View) {
        // TODO - Implement
    }
    //endregion
}