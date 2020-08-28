package room106.app.read.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R

class ChangeNameActivity : AppCompatActivity() {

    // Views
    private lateinit var toolBar: Toolbar
    private lateinit var nameEditText: EditText
    private lateinit var saveButton: Button

    private var currentName: String? = null

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_name)

        auth = Firebase.auth
        db = Firebase.firestore

        // Connect views
        toolBar = findViewById(R.id.toolBar)
        nameEditText = findViewById(R.id.nameEditText)
        saveButton = findViewById(R.id.saveButton)

        // Attach listeners
        nameEditText.addTextChangedListener(nameEditTextWatcher)
        toolBar.setNavigationOnClickListener(onClickBack)

        currentName = intent.getStringExtra("name")
        if (currentName != null) {
            nameEditText.setText(currentName)
            nameEditText.setSelection(currentName?.length ?: 0)
        }

        checkTypedName()
    }

    fun onClickSave(v: View) {
        val currentUserID = auth.currentUser?.uid ?: return

        if (checkTypedName()) {
            val typedName = nameEditText.text.toString()

            val userRef = db.collection("users").document(currentUserID)

            // All titles written by this user
            val userTitlesRef = db.collection("titles")
                .whereEqualTo("authorID", currentUserID)
                .orderBy("publicationTime", Query.Direction.DESCENDING)
                .limit(495)

            val userTitles = ArrayList<DocumentReference>()
            userTitlesRef.get().addOnSuccessListener { documents ->

                // Grab all titles written by this user
                for (document in documents) {
                    userTitles.add(document.reference)
                }

                db.runBatch { batch ->
                    // Update user's document
                    batch.update(userRef, "name", typedName)

                    // Update the last 500 titles written by this user containing "authorName" field
                    for (titleRef in userTitles) {
                        batch.update(titleRef, "authorName", typedName)
                    }
                } .addOnSuccessListener {
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkTypedName(): Boolean {
        val typedName = nameEditText.text.toString()
        val isNameValid = isNameValid(typedName)
        saveButton.isEnabled = isNameValid
        return isNameValid
    }

    private fun isNameValid(name: String): Boolean {
        // Name:
        //   - min two capital words
        //   - min 3 characters
        //   - max 30 characters
        val regex = getString(R.string.name_regex).toRegex()
        return name.length in 3..30 && name.matches(regex)
    }

    private val nameEditTextWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            checkTypedName()
        }
    }

    private val onClickBack = View.OnClickListener {
        finish()
    }
}