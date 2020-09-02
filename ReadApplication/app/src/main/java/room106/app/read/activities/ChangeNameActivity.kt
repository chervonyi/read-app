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

        // Attach listeners
        nameEditText.addTextChangedListener(nameEditTextWatcher)
        toolBar.setOnMenuItemClickListener(onClickSubmitListener)
        toolBar.setNavigationOnClickListener(onClickBack)

        currentName = intent.getStringExtra("name")
        if (currentName != null) {
            nameEditText.setText(currentName)
            nameEditText.setSelection(currentName?.length ?: 0)
        }

        checkTypedName()
    }

    private fun saveName() {
        val currentUserID = auth.currentUser?.uid ?: return

        if (checkTypedName()) {
            val typedName = nameEditText.text.toString()

            // Update user document
            val userRef = db.collection("users").document(currentUserID)
            userRef.update("name", typedName)

            // Update all titles written by this user
            val userTitlesRef = db.collection("titles")
                .whereEqualTo("authorID", currentUserID)
                .orderBy("publicationTime", Query.Direction.DESCENDING)

            // Update all titles written by this user that have been liked by someone
            val likedTitlesRef = db.collection("liked")
                .whereEqualTo("authorID", currentUserID)


            // Update all titles written by this user that have been saved by someone
            val savedTitlesRef = db.collection("saved")
                .whereEqualTo("authorID", currentUserID)

            executeUpdateNameQuery(userTitlesRef, typedName)
            executeUpdateNameQuery(likedTitlesRef, typedName)
            executeUpdateNameQuery(savedTitlesRef, typedName)
        }
    }

    private var executedQueriesNum = 0
    private fun executeUpdateNameQuery(query: Query, typedName: String) {

        query.get().addOnSuccessListener { documents ->
            for (document in documents) {
                document.reference.update("authorName", typedName)
            }
            executedQueriesNum += 1
            if (executedQueriesNum == 3) {
                Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun checkTypedName(): Boolean {
        val typedName = nameEditText.text.toString()
        val isNameValid = isNameValid(typedName)
        toolBar.menu.findItem(R.id.menuSubmitItem).isVisible = isNameValid
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

    private val onClickSubmitListener = Toolbar.OnMenuItemClickListener  {
        when (it.itemId) {
            R.id.menuSubmitItem -> {
                saveName()
                true
            }

            else -> false
        }
    }

    private val onClickBack = View.OnClickListener {
        finish()
    }
}