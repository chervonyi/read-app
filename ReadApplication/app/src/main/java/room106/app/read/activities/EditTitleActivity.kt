package room106.app.read.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
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

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var titleID: String? = null
    private var titleStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_title)

        // Connect views
        toolBar = findViewById(R.id.toolBar)
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        bodyEditText = findViewById(R.id.bodyEditText)
        saveTitleButton = findViewById(R.id.saveTitleButton)
        publishTitleButton = findViewById(R.id.publishTitleButton)

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
        }

        checkFields()
    }

    //region Load Title Data
    private fun loadTitleData() {
        if (titleID != null) {
            db.collection("titles").document(titleID!!).get()
                .addOnSuccessListener {document ->
                    val title = document.toObject(Title::class.java)

                    titleStatus = title?.status

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
            titleEditText.setText(title.title)
            descriptionEditText.setText(title.description)
            bodyEditText.setText(body)
        } else {
            titleEditText.setText("")
            descriptionEditText.setText("")
            bodyEditText.setText("")
        }
    }
    //endregion

    //region Click Listeners
    fun onClickSave(v: View) {
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
                                    titleStatus = "draft"
                                    checkFields()
                                }
                        }

                }
            }
    }

    private fun updateExistingTitle() {
        if (titleID != null) {
            val newTitle = titleEditText.text.toString()
            val newDescription = descriptionEditText.text.toString()
            val newBody = bodyEditText.text.toString()

            val titleRef = db.collection("titles").document(titleID!!)
            val bodyRef = titleRef.collection("body").document("text")

            var titleUpdated = false
            var bodyUpdated = false

            titleRef.update(mapOf(
                "title" to newTitle,
                "description" to newDescription
            )).addOnSuccessListener {
                titleUpdated = true

                if (bodyUpdated) {
                    saveTitleButton.text = getString(R.string.saved)
                }
            }

            bodyRef.update("text", newBody).addOnSuccessListener {
                bodyUpdated = true

                if (titleUpdated) {
                    saveTitleButton.text = getString(R.string.saved)
                }
            }
        }
    }

    fun onClickPublish(v: View) {
        if (titleID != null) {
            val titleRef = db.collection("titles").document(titleID!!)

            titleRef.update("status", "published")
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.published), Toast.LENGTH_SHORT).show()
                    titleStatus = "published"
                    checkFields()
                }
        }
    }

    private val onClickBackListener = View.OnClickListener {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
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
                titleStatus == "draft"
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
}
