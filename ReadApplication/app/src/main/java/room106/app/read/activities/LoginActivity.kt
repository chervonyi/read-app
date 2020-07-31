package room106.app.read.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import room106.app.read.R


class LoginActivity : AppCompatActivity() {

    // Views
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var showPasswordButton: ImageButton

    // Firebase
    private lateinit var auth: FirebaseAuth

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Connect views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        showPasswordButton = findViewById(R.id.showPasswordButton)

        emailEditText.addTextChangedListener(loginFormWatcher)
        passwordEditText.addTextChangedListener(loginFormWatcher)

        // Firebase
        auth = Firebase.auth

        if (auth.currentUser != null) {
            // User is Logged In -> Go to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        }
    }

    //region Click Listeners
    fun onClickLogIn(v: View) {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (isEmailValid(email) && isPasswordValid(password)) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Sign In success

                    // Go to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
                }
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.incorrect_login), Toast.LENGTH_LONG).show()
                }
        }
    }

    fun onClickGoToSignUp(v: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickForgotPassword(v: View) {
       val intent = Intent(this, ResetPasswordActivity::class.java)

        val email = emailEditText.text.toString()
        if (isEmailValid(email)) {
            intent.putExtra("typed_email", email)
        }

        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
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

    //region Form
    private val loginFormWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            loginButton.isEnabled = isValidateForm()
        }
    }

    private fun isValidateForm(): Boolean {
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()
        return  isEmailValid(email) && isPasswordValid(password)
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
}