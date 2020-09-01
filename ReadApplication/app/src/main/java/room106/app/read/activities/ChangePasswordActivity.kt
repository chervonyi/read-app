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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R

class ChangePasswordActivity : AppCompatActivity() {

    // Views
    private lateinit var toolBar: Toolbar
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmNewPasswordEditText: EditText

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = Firebase.auth
        db = Firebase.firestore

        // Connect views
        toolBar = findViewById(R.id.toolBar)
        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPasswordEditText)

        // Attach listeners
        oldPasswordEditText.addTextChangedListener(passwordEditTextWatcher)
        newPasswordEditText.addTextChangedListener(passwordEditTextWatcher)
        confirmNewPasswordEditText.addTextChangedListener(passwordEditTextWatcher)
        toolBar.setNavigationOnClickListener(onClickBack)
        toolBar.setOnMenuItemClickListener(onClickSubmitListener)

        toolBar.menu.findItem(R.id.menuSubmitItem).isVisible = false
    }

    private fun savePassword() {
        val currentUser = auth.currentUser ?: return

        if (isFormValid() && currentUser.email != null) {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()

            val credential = EmailAuthProvider
                .getCredential(currentUser.email!!, oldPassword)

            currentUser.reauthenticate(credential).addOnSuccessListener {
                // User has been recently Login (required for security changes e.g. password changing)
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.password_updated), Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun isFormValid(): Boolean {
        val oldPassword = oldPasswordEditText.text.toString()
        val newPassword = newPasswordEditText.text.toString()
        val confirmNewPassword = confirmNewPasswordEditText.text.toString()

        return isPasswordValid(oldPassword) &&
                isPasswordValid(newPassword) &&
                isPasswordValid(confirmNewPassword) &&
                oldPassword != newPassword &&
                newPassword == confirmNewPassword
    }

    private fun isPasswordValid(password: String): Boolean {
        // Password:
        //   - min 5 chars
        //   - max 20 chars
        return password.length in 5..20
    }

    private val passwordEditTextWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            toolBar.menu.findItem(R.id.menuSubmitItem).isVisible = isFormValid()
        }
    }

    private val onClickSubmitListener = Toolbar.OnMenuItemClickListener  {
        when (it.itemId) {
            R.id.menuSubmitItem -> {
                savePassword()
                true
            }

            else -> false
        }
    }

    private val onClickBack = View.OnClickListener {
        finish()
    }
}