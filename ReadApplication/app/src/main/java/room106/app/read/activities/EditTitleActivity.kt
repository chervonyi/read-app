package room106.app.read.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.tasks.OnSuccessListener
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
    private lateinit var saveTitleButton: Button
    private lateinit var publishTitleButton: Button

    private lateinit var titleSkeleton: View
    private lateinit var descriptionSkeleton: View
    private lateinit var bodySkeleton: View

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var titleID: String? = null
    private var isPublished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_title)

        // Connect views
        toolBar =               findViewById(R.id.toolBar)
        titleEditText =         findViewById(R.id.titleEditText)
        descriptionEditText =   findViewById(R.id.descriptionEditText)
        bodyEditText =          findViewById(R.id.bodyEditText)
        saveTitleButton =       findViewById(R.id.saveTitleButton)
        publishTitleButton =    findViewById(R.id.publishTitleButton)
        titleSkeleton =         findViewById(R.id.titleSkeleton)
        descriptionSkeleton =   findViewById(R.id.descriptionSkeleton)
        bodySkeleton =          findViewById(R.id.titleBodySkeleton)

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
            saveTitleButton.text = getString(R.string.save)
        } else {
            // User's going to create a new title
            saveTitleButton.text = getString(R.string.save_draft)
            skeletonIsShow = false
        }

        checkFields()
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

        skeletonIsShow = false
    }
    //endregion

    //region Click Listeners
    fun onClickSave(v: View) {
        if (skeletonIsShow) { return }

        val currentUser = auth.currentUser ?: return

        if (titleID == null) {
            // Create a new document in "titles" collection
            saveNewTitle(currentUser)
        } else {
            // Edit an existing document in "titles" collection
            updateExistingTitle()
        }
    }

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
                                    saveTitleButton.text = getString(R.string.saved)
                                    isPublished = false
                                    checkFields()
                                }
                        }

                }
            }
    }

    private fun updateExistingTitle() {
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
            )).addOnSuccessListener(savingSuccessListener)

            // Update body text in subcollection
            bodyRef.update("text", bodyEditText.text.toString())
                .addOnSuccessListener(savingSuccessListener)

            if (isPublished) {
                // Update this title data in "liked" collection
                val likedTitlesRef = db.collection("liked")
                    .whereEqualTo("titleID", titleID)

                // Update this title data in "saved" collection
                val savedTitlesRef = db.collection("saved")
                    .whereEqualTo("titleID", titleID)

                val updates = mapOf(
                    "title" to title,
                    "description" to description
                )

                executeUpdateTitleData(likedTitlesRef, updates)
                executeUpdateTitleData(savedTitlesRef, updates)
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

    private var savingParts = 0
    private val savingSuccessListener = OnSuccessListener<Void> {
        savingParts += 1

        if (savingParts == 2) {
            savingParts = 0
            saveTitleButton.text = getString(R.string.saved)
        }
    }

    fun onClickPublish(v: View) {
        if (titleID != null && !skeletonIsShow) {
            updateExistingTitle()

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

                    Toast.makeText(this, getString(R.string.published), Toast.LENGTH_SHORT).show()
                    isPublished = true
                    checkFields()
                }
        }
    }

    private fun incrementUserTitlesCount() {
        val currentUserID = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserID)
            .update("titlesCount", FieldValue.increment(1))
    }

    private val onClickBackListener = View.OnClickListener {
        finish()
    }
    //endregion

    //region Fields validation
    private val titleDataWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
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

    private fun checkFields() {
        val minLengthToSave = 10

        saveTitleButton.isEnabled = titleEditText.text.length > minLengthToSave ||
                descriptionEditText.text.length > minLengthToSave ||
                bodyEditText.text.length > minLengthToSave

        publishTitleButton.visibility = if (isValidForPublish()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    //endregion

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

        // Data panels
        titleEditText.visibility =          dataVisibility
        descriptionEditText.visibility =    dataVisibility
        bodyEditText.visibility =           dataVisibility
    }
}
