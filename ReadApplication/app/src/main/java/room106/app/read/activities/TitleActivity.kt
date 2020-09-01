package room106.app.read.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
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
import room106.app.read.views.LikeButton
import room106.app.read.views.MainButton
import room106.app.read.views.TitleBodyLinearLayout
import room106.app.read.views.TitleStatsPanelView


class TitleActivity : AppCompatActivity() {

    // Views
    private lateinit var toolBar: Toolbar
    private lateinit var mainNestedScrollView: NestedScrollView
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var headerTextView: TextView
    private lateinit var authorAvatarImageView: RoundedImageView
    private lateinit var authorTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var titleBodyLinearLayout: TitleBodyLinearLayout
    private lateinit var likeButton: LikeButton
    private lateinit var saveTitleButton: MainButton
    private lateinit var titleStatsPanel: TitleStatsPanelView

    private lateinit var titleHeaderSkeleton: View
    private lateinit var titleAuthorSkeleton: View
    private lateinit var titleDescriptionSkeleton: LinearLayout
    private lateinit var titleBodySkeleton: LinearLayout

    // Firebase
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var titleID: String? = null
    private var title: Title? = null

    private val CHARACTERS_PER_MINUTE = 300

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        // Connect views
        toolBar =                   findViewById(R.id.toolBar)
        mainNestedScrollView =      findViewById(R.id.mainNestedScrollView)
        appBarLayout =              findViewById(R.id.appBarLayout)
        headerTextView =            findViewById(R.id.titleHeaderTextView)
        authorAvatarImageView =     findViewById(R.id.titleAuthorAvatarImageView)
        authorTextView =            findViewById(R.id.titleAuthorTextView)
        descriptionTextView =       findViewById(R.id.titleDescriptionTextView)
        likeButton =                findViewById(R.id.likeButton)
        saveTitleButton =           findViewById(R.id.saveTitleButton)
        titleStatsPanel =           findViewById(R.id.titleStatsPanel)
        titleHeaderSkeleton =       findViewById(R.id.titleHeaderSkeleton)
        titleAuthorSkeleton =       findViewById(R.id.titleAuthorSkeleton)
        titleDescriptionSkeleton =  findViewById(R.id.titleDescriptionSkeleton)
        titleBodySkeleton =         findViewById(R.id.titleBodySkeleton)
        titleBodyLinearLayout =     findViewById(R.id.titleBodyLinearLayout)

        // Assign listeners
        authorAvatarImageView.setOnClickListener(onClickTitleAuthor)
        authorTextView.setOnClickListener(onClickTitleAuthor)
        toolBar.setOnMenuItemClickListener(onClickMenuListener)
        toolBar.setNavigationOnClickListener(onClickBackListener)

        if (intent.hasExtra("title_id")) {
            titleID = intent.getStringExtra("title_id")
        }

        db = Firebase.firestore
        auth = Firebase.auth

        // onStart???
        loadTitle()
        checkIfLiked()
        checkIfSaved()
        titleBodyLinearLayout.removeAllViews()

        skeletonIsShow = true
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
            // Assign data
            headerTextView.text = title.title
            authorTextView.text = title.authorName
            descriptionTextView.text = title.description
            titleBodyLinearLayout.setBodyText(body)

            var timeToRead = body.length / CHARACTERS_PER_MINUTE
            if (timeToRead <= 0) {
                timeToRead =  1
            }

            val avatarName = "ic_avatar_${title.authorAvatar}"
            val image = resources.getIdentifier(avatarName, "drawable", packageName)
            authorAvatarImageView.setImageResource(image)

            titleStatsPanel.attachData(title.readsCount, title.likesCount, timeToRead)
            skeletonIsShow = false
        }
    }
    //endregion

    //region ToolBar
    private val onClickBackListener = View.OnClickListener {
        finish()
    }

    private val onClickMenuListener = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.titleMenuEdit -> {
                onClickEditTitle()
                true
            }

            else -> false
        }
    }

    private fun onClickEditTitle() {
        if (titleID != null && !skeletonIsShow) {
            val intent = Intent(this, EditTitleActivity::class.java)
            intent.putExtra("title_id", titleID)
            startActivity(intent)
        }
    }
    //endregion

    //region Like
    fun onClickLike(v: View) {
        if (title != null && titleID != null && !skeletonIsShow) {

            val currentUserID = auth.currentUser?.uid

            if (currentUserID == null) {
                // Offer to login
                val intent = Intent(this, OfferToLoginActivity::class.java)
                startActivity(intent)
                finish()

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
                likeButton.isLiked = true
                likeButton.text = getString(R.string.you_liked_it)
            } else {
                likeButton.isLiked = false
                likeButton.text = getString(R.string.like)
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
        if (title != null && titleID != null && !skeletonIsShow) {

            val currentUserID = auth.currentUser?.uid

            if (currentUserID == null) {
                // Offer to login
                val intent = Intent(this, OfferToLoginActivity::class.java)
                startActivity(intent)
                finish()

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

    private val onClickTitleAuthor = View.OnClickListener {
        if (title != null && !skeletonIsShow) {
            val intent = Intent(this, UserActivity::class.java)
            intent.putExtra("user_id", title?.authorID)
            startActivity(intent)
        }
    }

    fun onClickScrollTop(v: View) {
        mainNestedScrollView.fullScroll(ScrollView.FOCUS_UP)
        appBarLayout.setExpanded(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        titleBodyLinearLayout.destroyAds()
    }

    private var _skeletonIsShown = false
    var skeletonIsShow: Boolean
        get() = _skeletonIsShown
        set(value) {
            _skeletonIsShown = value

            if (value) {
                // Show skeleton
                manageVisibility(View.VISIBLE, View.GONE)
            } else {
                manageVisibility(View.GONE, View.VISIBLE)
            }
        }

    private fun manageVisibility(skeletonVisibility: Int, dataVisibility: Int) {
        // Skeleton panels
        titleHeaderSkeleton.visibility =        skeletonVisibility
        titleAuthorSkeleton.visibility =        skeletonVisibility
        titleDescriptionSkeleton.visibility =   skeletonVisibility
        titleBodySkeleton.visibility =          skeletonVisibility

        // Data panels
        headerTextView.visibility =         dataVisibility
        authorTextView.visibility =         dataVisibility
        descriptionTextView.visibility =    dataVisibility
        titleBodyLinearLayout.visibility =  dataVisibility
    }
}