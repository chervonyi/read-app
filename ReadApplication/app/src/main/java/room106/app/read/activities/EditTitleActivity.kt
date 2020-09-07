package room106.app.read.activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R
import room106.app.read.models.Title
import room106.app.read.models.User
import java.util.*

class EditTitleActivity : AppCompatActivity() {

    // Views
    private lateinit var toolBar: Toolbar
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var bodyEditText: EditText

    private lateinit var titleSkeleton: View
    private lateinit var descriptionSkeleton: View
    private lateinit var bodySkeleton: View
    private lateinit var bodySkeletonList: View

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var titleID: String? = null
    private var isPublished = false
    private var isSaved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_title)

        // Connect views
        toolBar =               findViewById(R.id.toolBar)
        titleEditText =         findViewById(R.id.titleEditText)
        descriptionEditText =   findViewById(R.id.descriptionEditText)
        bodyEditText =          findViewById(R.id.bodyEditText)
        titleSkeleton =         findViewById(R.id.titleSkeleton)
        descriptionSkeleton =   findViewById(R.id.descriptionSkeleton)
        bodySkeleton =          findViewById(R.id.bodySkeleton)
        bodySkeletonList =      findViewById(R.id.titleBodySkeleton)

        // Set "Done" button on keyboard
        titleEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        titleEditText.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        descriptionEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        descriptionEditText.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)

        // Connect listeners
        titleEditText.addTextChangedListener(titleDataWatcher)
        descriptionEditText.addTextChangedListener(titleDataWatcher)
        bodyEditText.addTextChangedListener(titleDataWatcher)
        toolBar.setNavigationOnClickListener(onClickBackListener)
        toolBar.setOnMenuItemClickListener(onClickMenuListener)

        // Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        skeletonIsShow = true

        titleID = intent.getStringExtra("title_id")
    }

    override fun onStart() {
        super.onStart()

        if (titleID != null) {
            // User editing an existing title
            loadTitleData()
        } else {
            // User's going to create a new title
            skeletonIsShow = false
            checkFields()
        }
    }

    //region Load Title Data
    private fun loadTitleData() {
        if (titleID != null) {
            db.collection("titles").document(titleID!!).get()
                .addOnSuccessListener {document ->
                    val title = document.toObject(Title::class.java)

                    isPublished = title?.status == "published"

                    document.reference.collection("body").document("text")
                        .get().addOnSuccessListener { bodyDocument ->
                            val body = bodyDocument.get("text").toString()
                            updateTitleUI(title, body)
                        }
                        .addOnFailureListener {
                            showToast(getString(R.string.loading_error))
                            finish()
                        }
                }
                .addOnFailureListener {
                    showToast(getString(R.string.loading_error))
                    finish()
                }
        }
    }

    private fun updateTitleUI(title: Title?, body: String) {
        // Set data
        if (title != null) {
            titleEditText.setText(title.title)
            descriptionEditText.setText(title.description)
            bodyEditText.setText(body)
        } else {
            titleEditText.setText("")
            descriptionEditText.setText("")
            bodyEditText.setText("")
        }

        isSaved = true
        skeletonIsShow = false
        checkFields()
    }
    //endregion

    //region Save Click Listener
    private fun onClickSave() {
        if (skeletonIsShow) { return }

        val currentUser = auth.currentUser ?: return

        if (titleID == null) {
            // Create a new document in "titles" collection
            saveNewTitle(currentUser)
        } else {
            // Edit an existing document in "titles" collection
            updateExistingTitle(true)
        }
    }

    // Saving #1 Case
    private fun saveNewTitle(currentUser: FirebaseUser) {
        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val titleBody = bodyEditText.text.toString()
        val status = "draft"
        val readsCount = 0
        val likesCount = 0

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)

                if (user != null) {
                    val authorName = user.name
                    val authorID = currentUser.uid
                    val authorAvatar = user.avatar
                    val time = Timestamp(Date())

                    val titleObject = Title(title, description, authorName, authorID,
                        authorAvatar, status, time, time, readsCount, likesCount)

                    db.collection("titles").add(titleObject)
                        .addOnSuccessListener { titleDocument ->
                            val bodyObject = hashMapOf(
                                "text" to titleBody
                            )

                            titleDocument.collection("body").document("text")
                                .set(bodyObject).addOnSuccessListener {
                                    titleID = titleDocument.id
                                    isSaved = true
                                    isPublished = false
                                    checkFields()
                                    showToast(getString(R.string.saved))
                                }
                                .addOnFailureListener {
                                    showToast(getString(R.string.loading_error))
                                }
                        }
                        .addOnFailureListener {
                            showToast(getString(R.string.loading_error))
                        }
                }
            }
            .addOnFailureListener {
                showToast(getString(R.string.loading_error))
            }
    }

    // Saving #2 Case
    private fun updateExistingTitle(showToast: Boolean) {
        val currentUserID = auth.currentUser?.uid ?: return

        if (titleID != null) {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()

            val titleRef = db.collection("titles").document(titleID!!)
            val bodyRef = titleRef.collection("body").document("text")

            // Update title document
            titleRef.update(mapOf(
                "title" to title,
                "description" to description,
                "lastTimeUpdated" to Timestamp(Date())
            )).addOnSuccessListener {

                // Update body text in subcollection
                bodyRef.update("text", bodyEditText.text.toString())
                    .addOnSuccessListener {

                        isSaved = true
                        checkFields()

                        if (showToast) {
                            showToast(getString(R.string.saved))
                        }

                        if (isPublished) {
                            // Update this title data in "liked" collection
                            val likedTitlesRef = db.collection("liked")
                                .whereEqualTo("authorID", currentUserID)
                                .whereEqualTo("titleID", titleID)

                            // Update this title data in "saved" collection
                            val savedTitlesRef = db.collection("saved")
                                .whereEqualTo("authorID", currentUserID)
                                .whereEqualTo("titleID", titleID)

                            val updates = mapOf(
                                "title" to title,
                                "description" to description
                            )

                            executeUpdateTitleData(likedTitlesRef, updates)
                            executeUpdateTitleData(savedTitlesRef, updates)
                        }
                    }
                    .addOnFailureListener {
                        showToast(getString(R.string.loading_error))
                    }
                }
                .addOnFailureListener {
                    showToast(getString(R.string.loading_error))
                }
        }
    }

    private fun executeUpdateTitleData(query: Query, updates: Map<String, String>) {
        query.get().addOnSuccessListener { documents ->
            documents.forEach { document ->
                document.reference.update(updates)
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
    //endregion

    //region Publish Click Listener
    private fun onClickPublish() {
        if (titleID != null && !skeletonIsShow) {
            showOfferToPublishTitleDialog()
        }
    }

    private fun publishTitle() {
        updateExistingTitle(false)

        val titleRef = db.collection("titles").document(titleID!!)

        // Make search flags
        val title = titleEditText.text.toString().toLowerCase(Locale.getDefault())
        val words = title.split(" ")

        if (words.isNotEmpty()) {
            titleRef.update("flags", words)
        }

        // Change title status
        val updates = mapOf(
            "status" to "published",
            "publicationTime" to Timestamp(Date())
        )
        titleRef.update(updates)
            .addOnSuccessListener {
                incrementUserTitlesCount()

                showToast(getString(R.string.published))
                finish()
            }
            .addOnFailureListener {
                showToast(getString(R.string.loading_error))
            }
    }

    private fun showOfferToPublishTitleDialog() {
        val dialogClickListener = DialogInterface.OnClickListener { _, i ->
            if (i ==  DialogInterface.BUTTON_POSITIVE) {
                publishTitle()
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Publish this title?").setPositiveButton("Publish", dialogClickListener)
            .setNegativeButton("Cancel", dialogClickListener).show()
    }

    private fun incrementUserTitlesCount() {
        val currentUserID = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserID)
            .update("titlesCount", FieldValue.increment(1))
    }
    //endregion

    //region Other Listeners
    private val onClickMenuListener = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.menuSaveItem -> {
                onClickSave()
                true
            }

            R.id.menuPublishItem -> {
                onClickPublish()
                true
            }
            else -> false
        }
    }

    private val onClickBackListener = View.OnClickListener {
        if (!isSaved && isValidForSave()) {
            showOfferToSaveTitleDialog()
        } else {
            finish()
        }
    }

    private fun showOfferToSaveTitleDialog() {
        val dialogClickListener = DialogInterface.OnClickListener { _, i ->
            if (i ==  DialogInterface.BUTTON_POSITIVE) {
                // Save title
                onClickSave()
            } else if (i == DialogInterface.BUTTON_NEGATIVE) {
                finish()
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Save changes?").setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }
    //endregion

    //region Fields validation
    private val titleDataWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            isSaved = false
            checkFields()
        }
    }

    private fun isValidForPublish(): Boolean {
        return isTitleValid() &&
                isDescriptionValid() &&
                isBodyValid() &&
                titleID != null &&
                !isPublished
    }

    private fun isTitleValid(): Boolean {
        return titleEditText.text.length in
                resources.getInteger(R.integer.titleMinLength)..
                resources.getInteger(R.integer.titleMaxLength)
    }

    private fun isDescriptionValid(): Boolean {
        return descriptionEditText.text.length in
                resources.getInteger(R.integer.descriptionMinLength)..
                resources.getInteger(R.integer.descriptionMaxLength)
    }

    private fun isBodyValid(): Boolean {
        return bodyEditText.text.length in
                resources.getInteger(R.integer.titleBodyMinLength)..
                resources.getInteger(R.integer.titleBodyMaxLength)
    }

    private fun isValidForSave(): Boolean {
        val minLengthToSave = 10
        return (titleEditText.text.length > minLengthToSave ||
                descriptionEditText.text.length > minLengthToSave ||
                bodyEditText.text.length > minLengthToSave)
    }

    private fun checkFields() {
        toolBar.menu.findItem(R.id.menuSaveItem).isVisible = !isSaved && isValidForSave()
        toolBar.menu.findItem(R.id.menuPublishItem).isVisible = isValidForPublish()
    }
    //endregion

    //region Skeleton
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
        titleSkeleton.visibility =          skeletonVisibility
        descriptionSkeleton.visibility =    skeletonVisibility
        bodySkeleton.visibility =           skeletonVisibility
        bodySkeletonList.visibility =       skeletonVisibility

        // Data panels
        titleEditText.visibility =          dataVisibility
        descriptionEditText.visibility =    dataVisibility
        bodyEditText.visibility =           dataVisibility
    }
    //endregion
}
