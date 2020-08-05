package room106.app.read.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.models.Title

class TitleActivity : AppCompatActivity() {

    // Views
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
               updateTitleUI(title)
           }
       }
    }

    private fun updateTitleUI(title: Title?) {
        if (title != null) {
            headerTextView.text = title.title
            authorTextView.text = title.authorName
            descriptionTextView.text = title.description
            readsCountTextView.text = title.readsCount.toString()
            likesCountTextView.text = title.likesCount.toString()

            // TODO - timeToReadTextView.text = body.length / 100 ...
            // TODO - Assign title body

            val avatarName = "ic_avatar_${title.authorAvatar}"
            val image = resources.getIdentifier(avatarName, "drawable", packageName)
            authorAvatarImageView.setImageResource(image)
        }
    }

    //region Listeners
    fun onClickBack(v: View) {
        // TODO - Implement
    }

    fun onClickMore(v: View) {
        // TODO - Implement
    }
    //endregion
}