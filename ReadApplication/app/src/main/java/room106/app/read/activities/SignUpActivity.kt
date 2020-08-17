package room106.app.read.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.R
import room106.app.read.models.User

class SignUpActivity : AppCompatActivity() {

    // Views
    private lateinit var toolBar: Toolbar
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var createAccountButton: Button
    private lateinit var showPasswordButton: ImageButton

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Connect views
        toolBar = findViewById(R.id.toolBar)
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        createAccountButton = findViewById(R.id.createAccountButton)
        showPasswordButton = findViewById(R.id.showPasswordButton)

        // Set listeners
        nameEditText.addTextChangedListener(signUpFormWatcher)
        emailEditText.addTextChangedListener(signUpFormWatcher)
        passwordEditText.addTextChangedListener(signUpFormWatcher)
        toolBar.setNavigationOnClickListener(onClickBackListener)

        // Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        if (auth.currentUser != null) {
            // User is Logged In -> Go to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        }
    }

    //region Click Listeners
    fun onClickCreateAccount(v: View) {

        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (isNameValid(name) && isEmailValid(email) && isPasswordValid(password)) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Sign In success

                    val userInstance = getDefaultUserData(name)
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        db.collection("users").document(currentUser.uid).set(userInstance)
                            .addOnSuccessListener {
                                // Registration successful
                                Log.d(TAG, "Registration successful")

                                // Go to ChangeAvatarActivity
                                val intent = Intent(this, ChangeAvatarActivity::class.java)
                                intent.putExtra("purpose", "sign_up")
                                startActivity(intent)
                                finish()
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, getString(R.string.failed_to_register), Toast.LENGTH_LONG).show()
                            }
                    }
                }

                .addOnFailureListener {
                    // Failed to register new user
                    Toast.makeText(this, getString(R.string.failed_to_register), Toast.LENGTH_LONG).show()
                }
        }
    }

    fun onClickShowPrivacyPolicy(v: View) {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickGoToLogin(v: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    private val onClickBackListener = View.OnClickListener {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    fun onClickShowHidePassword(v: View) {
        isPasswordVisible = !isPasswordVisible

        val cursorPosition = passwordEditText.selectionEnd

        if (isPasswordVisible) {
            // Show password
            showPasswordButton.setImageResource(R.drawable.ic_eye_off)
            passwordEditText.transformationMethod = HideReturnsTransformationMethod()
            passwordEditText.setSelection(cursorPosition)
        } else {
            // Hide password
            showPasswordButton.setImageResource(R.drawable.ic_eye)
            passwordEditText.transformationMethod = PasswordTransformationMethod()
            passwordEditText.setSelection(cursorPosition)
        }
    }
    //endregion

    //region Sign Up
    private fun getDefaultUserData(name: String): User {
        // DEFAULT VALUES:
        val avatar = 0
        val isPaid = false
        val titlesCount = 0
        val followersCount = 0
        val likesCount = 0

        val timestamp = Timestamp.now()

        return User(name, avatar, isPaid, timestamp, titlesCount, followersCount, likesCount )
    }

    private val signUpFormWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            createAccountButton.isEnabled = isValidateForm()
        }
    }

    private fun isValidateForm(): Boolean {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        return isNameValid(name) && isEmailValid(email) && isPasswordValid(password)
    }

    private fun isNameValid(name: String): Boolean {
        val regex = getString(R.string.name_regex).toRegex()
        // Name:
        //   - min two capital words
        //   - min 3 characters
        //   - max 30 characters
        return name.length in 3..30 && name.matches(regex)
    }

    private fun isEmailValid(email: String): Boolean {
        // Email:
        //   - email pattern
        //   - min 4 characters
        //   - max 30 characters
        return email.length in 4..30 && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        // Password:
        //   - min 5 chars
        //   - max 20 chars
        return password.length in 5..20
    }
    //endregion

    companion object {
        const val TAG = "SignUpActivity"
    }
}