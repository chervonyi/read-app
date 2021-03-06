package room106.app.read.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.models.Title
import room106.app.read.views.LikeButton
import room106.app.read.views.ChameleonButton
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
    private lateinit var saveTitleButton: ChameleonButton
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
    private var menuIsSet = false
    private var isPublished = false

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
    }

    override fun onStart() {
        super.onStart()

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
                       showHideMenu(title)
                       updateTitleUI(title, body)
                   }
                   .addOnFailureListener {
                       showErrorToast()
                       finish()
                   }

               // Increment readsCount
               titleRef.update("readsCount", FieldValue.increment(1))
           }
               .addOnFailureListener {
                   showErrorToast()
                   finish()
               }
       }
    }

    private fun updateTitleUI(title: Title?, body: String) {
        this.title = title

        if (title != null) {
            // Assign data
            headerTextView.text = title.title
            authorTextView.text = title.authorName
            descriptionTextView.text = title.description
            titleBodyLinearLayout.setBodyText(body, title.status == "published")
            isPublished = title.status == "published"

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

    private fun showHideMenu(title: Title?) {
        if (menuIsSet) { return }
        val currentUserID = auth.currentUser?.uid ?: return

        if (currentUserID == title?.authorID) {
            // Show menu
            toolBar.inflateMenu(R.menu.title_current_user_author_menu)
        }

        menuIsSet = true
    }

    private val onClickMenuListener = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.titleMenuEdit -> {
                onClickEditTitle()
                true
            }

            R.id.titleMenuDelete -> {
                showOfferToDeleteTitleDialog()
                true
            }

            else -> false
        }
    }

    private fun deleteTitle() {
        val currentUserID = auth.currentUser?.uid ?: return

        if (title != null && titleID != null && !skeletonIsShow) {

            db.collection("titles").document(titleID!!)
                .update("status", "deleted")
                .addOnSuccessListener {

                    if (isPublished) {
                        decrementTitlesCount()
                    }

                    val likedTitlesRef = db.collection("liked")
                        .whereEqualTo("authorID", currentUserID)
                        .whereEqualTo("titleID", titleID)

                    val savedTitlesRef = db.collection("saved")
                        .whereEqualTo("authorID", currentUserID)
                        .whereEqualTo("titleID", titleID)

                    executeDeletionQuery(likedTitlesRef)
                    executeDeletionQuery(savedTitlesRef)

                    Toast.makeText(this, getString(R.string.successfully_deleted), Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    showErrorToast()
                }
        }
    }

    private fun executeDeletionQuery(query: Query) {
        query.get().addOnSuccessListener {documents ->
            documents.forEach { document ->
                document.reference.delete()
            }
        }
    }

    private fun decrementTitlesCount() {
        val currentUserID = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserID)
            .update("titlesCount", FieldValue.increment(-1))
    }

    private fun showOfferToDeleteTitleDialog() {
        val dialogClickListener = DialogInterface.OnClickListener { _, i ->
            if (i ==  DialogInterface.BUTTON_POSITIVE) {
                deleteTitle()
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to delete this title?").setPositiveButton("Delete", dialogClickListener)
            .setNegativeButton("Cancel", dialogClickListener).show()
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
            }

            // Check if title is published. If it is not -> do not like
            if (!isPublished) {
                Toast.makeText(this, getString(R.string.title_not_published), Toast.LENGTH_LONG).show()
                return
            }

            if (likeDocID != null) {
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
                    .addOnFailureListener {
                        showErrorToast()
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
                    .addOnFailureListener {
                        showErrorToast()
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
                .addOnFailureListener {
                    likeDocID = null
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
            }

            // Check if title is published. If it is not -> do not like
            if (!isPublished) {
                Toast.makeText(this, getString(R.string.title_not_published), Toast.LENGTH_LONG).show()
                return
            }

            if (saveDocID != null) {
                // Title has already been saved
                db.collection("saved").document(saveDocID!!).delete()
                    .addOnSuccessListener {
                        saveDocID = null
                    }
                    .addOnFailureListener {
                        showErrorToast()
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
                    .addOnFailureListener {
                        showErrorToast()
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

            if (newValue == null) {
                saveTitleButton.text = getString(R.string.save)
                saveTitleButton.isAccentButtonColor = true
            } else {
                saveTitleButton.text = getString(R.string.you_saved_it)
                saveTitleButton.isAccentButtonColor = false
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
                .addOnFailureListener {
                    saveDocID = null
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

    private fun showErrorToast() {
        Toast.makeText(this, getString(R.string.loading_error), Toast.LENGTH_LONG).show()
    }
}