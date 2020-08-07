package room106.app.read.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
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
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var bodyEditText: EditText
    private lateinit var saveTitleButton: Button
    private lateinit var publishTitleButton: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var titleID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_title)

        // Connect views
        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        bodyEditText = findViewById(R.id.bodyEditText)
        saveTitleButton = findViewById(R.id.saveTitleButton)
        publishTitleButton = findViewById(R.id.publishTitleButton)

        // TODO - Add "Done" button for EditText while multiline is available using:
//        setImeOptions(EditorInfo.IME_ACTION_DONE);
//        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        // Connect listeners
        titleEditText.addTextChangedListener(titleDataWatcher)
        descriptionEditText.addTextChangedListener(titleDataWatcher)
        bodyEditText.addTextChangedListener(titleDataWatcher)


        titleID = intent.getStringExtra("title_id")

        if (titleID != null) {
            // User editing an existing title
            // TODO - Load title data and set it
        } else {
            // User's going to create a new title
            saveTitleButton.text = getString(R.string.save_draft)
        }

        // Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        checkFields()
    }

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

    //region Fields validation
    private val titleDataWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            checkFields()
        }
    }

    private fun isFieldsValid(): Boolean {
        return isTitleValid() && isDescriptionValid() && isBodyValid()
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


        publishTitleButton.visibility = if (isTitleValid() &&
            isDescriptionValid() &&
            isBodyValid()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    //endregion

    fun onClickPublish(v: View) {
        // TODO - Implement
    }

    fun onClickMore(v: View) {
        // TODO - Implement
    }

    fun onClickBack(v: View) {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    companion object {
        const val TAG = "EditTitleActivity"
    }
}
