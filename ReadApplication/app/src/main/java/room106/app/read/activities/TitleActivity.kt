package room106.app.read.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.models.Title
import room106.app.read.views.MainButton


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
    private lateinit var likeButton: Button
    private lateinit var saveTitleButton: MainButton

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var titleID: String? = null
    private var title: Title? = null

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
        likeButton = findViewById(R.id.likeButton)
        saveTitleButton = findViewById(R.id.saveTitleButton)

        // Assign listeners
        authorAvatarImageView.setOnClickListener(onClickTitleAuthor)
        authorTextView.setOnClickListener(onClickTitleAuthor)

        if (intent.hasExtra("title_id")) {
            titleID = intent.getStringExtra("title_id")
        }

        db = Firebase.firestore
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        loadTitle()
        checkIfLiked()
        checkIfSaved()
    }

    //region Load Title
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

           // Increment readsCount
           titleRef.update("readsCount", FieldValue.increment(1))
       }
    }

    private fun updateTitleUI(title: Title?, body: String) {
        this.title = title

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
    //endregion

    //region Listeners
    private val onClickTitleAuthor = View.OnClickListener {
        if (title != null) {
            val intent = Intent(this, UserActivity::class.java)
            intent.putExtra("user_id", title?.authorID)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        }
    }

    fun onClickBack(v: View) {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    fun onClickScrollTop(v: View) {
        mainNestedScrollView.fullScroll(ScrollView.FOCUS_UP)
        appBarLayout.setExpanded(true)
    }

    fun onClickMore(v: View) {
        if (title != null && titleID != null) {
            val currentUser = auth.currentUser

            if (currentUser != null && currentUser.uid == title!!.authorID) {
                // Show Current User menu
                showCurrentUserMenu(v)
            } else {
                // Show simple menu
                showSimpleMenu(v)
            }
        }
    }

    private fun showCurrentUserMenu(v: View) {
        val menu = PopupMenu(this, v)

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.titleMenuEdit -> {
                    onClickEditTitle()
                    true
                }

                else -> false
            }
        }
        menu.inflate(R.menu.title_current_user_author_menu)
        menu.show()
    }

    private fun showSimpleMenu(v: View) {
        // TODO - Show simple menu
        // If there is no item in simple menu then Hide/Show menu button
    }

    private fun onClickEditTitle() {
        val intent = Intent(this, EditTitleActivity::class.java)
        intent.putExtra("title_id", titleID)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    //endregion

    //region Like
    fun onClickLike(v: View) {
        if (title != null && titleID != null) {

            val currentUserID = auth.currentUser?.uid

            if (currentUserID == null) {
                // Offer to login
                val intent = Intent(this, OfferToLoginActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)

            } else if (likeDocID != null) {
                // Title has already been liked -> Do unlike
                // TODO - Make a transaction
                db.collection("liked").document(likeDocID!!).delete()
                    .addOnSuccessListener {
                        likeDocID = null

                        // Decrement likesCount in title document
                        db.collection("titles").document(titleID!!)
                            .update("likesCount", FieldValue.increment(-1))

                        // Decrement likesCount in user document
                        db.collection("users").document(title!!.authorID)
                            .update("likesCount", FieldValue.increment(-1))
                    }

            } else {
                // Like title
                val likeDoc = hashMapOf(
                    "userID" to currentUserID,
                    "titleID" to titleID,
                    "time" to Timestamp.now(),
                    "title" to title!!.title,
                    "description" to title!!.description,
                    "authorID" to title!!.authorID,
                    "authorName" to title!!.authorName,
                    "authorAvatar" to title!!.authorAvatar
                )

                // TODO - Make a transaction
                db.collection("liked").add(likeDoc)
                    .addOnSuccessListener {
                        likeDocID = it.id

                        // Increment likesCount in title document
                        db.collection("titles").document(titleID!!)
                            .update("likesCount", FieldValue.increment(1))

                        // Increment likesCount in user document
                        db.collection("users").document(title!!.authorID)
                            .update("likesCount", FieldValue.increment(1))
                    }
            }
        }
    }

    private var _likeDocID: String? = null

    private var likeDocID: String?
        get() {
            return _likeDocID
        }
        set(newValue) {
            _likeDocID = newValue

            if (newValue != null) {
                likeButton.setBackgroundResource(R.drawable.simple_button)
                likeButton.text = getString(R.string.you_liked_it)
                likeButton.setTextColor(ContextCompat.getColor(this, R.color.colorSimpleButtonText))
            } else {
                likeButton.setBackgroundResource(R.drawable.like_button)
                likeButton.text = getString(R.string.like)
                likeButton.setTextColor(ContextCompat.getColor(this, R.color.colorFontMainButton))
            }
        }

    private fun checkIfLiked() {
        val currentUserID = auth.currentUser?.uid

        if (titleID != null && currentUserID != null) {
            db.collection("liked")
                .whereEqualTo("userID", currentUserID)
                .whereEqualTo("titleID", titleID)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.documents.size > 0) {
                        likeDocID = documents.documents[0].id
                    }
                }
        } else {
            likeDocID = null
        }
    }
    //endregion

    //region Save
    fun onClickSaveFavoriteTitle(v: View) {
        if (title != null && titleID != null) {

            val currentUserID = auth.currentUser?.uid

            if (currentUserID == null) {
                // Offer to login
                val intent = Intent(this, OfferToLoginActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)

            } else if (saveDocID != null) {
                // Title has already been saved
                db.collection("saved").document(saveDocID!!).delete()
                    .addOnSuccessListener {
                        saveDocID = null
                    }

            } else {
                // Save title
                val saveDoc = hashMapOf(
                    "userID" to currentUserID,
                    "titleID" to titleID,
                    "time" to Timestamp.now(),
                    "title" to title!!.title,
                    "description" to title!!.description,
                    "authorID" to title!!.authorID,
                    "authorName" to title!!.authorName,
                    "authorAvatar" to title!!.authorAvatar
                )

                db.collection("saved").add(saveDoc)
                    .addOnSuccessListener {
                        saveDocID = it.id
                    }
            }
        }
    }

    private var _saveDocID: String? = null

    private var saveDocID: String?
        get() {
            return _saveDocID
        }
        set(newValue) {
            _saveDocID = newValue

            if (newValue != null) {
                saveTitleButton.isFunctionActive = false
                saveTitleButton.text = getString(R.string.you_saved_it)
            } else {
                saveTitleButton.isFunctionActive = true
                saveTitleButton.text = getString(R.string.save)
            }
        }

    private fun checkIfSaved() {
        val currentUserID = auth.currentUser?.uid

        if (titleID != null && currentUserID != null) {
            db.collection("saved")
                .whereEqualTo("userID", currentUserID)
                .whereEqualTo("titleID", titleID)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.documents.size > 0) {
                        saveDocID = documents.documents[0].id
                    }
                }
        } else {
            saveDocID = null
        }
    }
    //endregion
}