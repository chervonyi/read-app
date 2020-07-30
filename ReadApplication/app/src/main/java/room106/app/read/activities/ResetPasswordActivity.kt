package room106.app.read.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import room106.app.read.R

class ResetPasswordActivity : AppCompatActivity() {

    // Views
    private lateinit var emailEditText: EditText
    private lateinit var resetPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        emailEditText = findViewById(R.id.emailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        emailEditText.addTextChangedListener(formFormWatcher)
    }

    override fun onStart() {
        super.onStart()

        if (intent.hasExtra("typed_email")) {
            val email = intent.getStringExtra("typed_email")
            if (email != null) {
                emailEditText.setText(email)
                resetPasswordButton.isEnabled = isEmailValid(email)
            }
        }

        resetPasswordButton.text = getString(R.string.reset_password_button)
    }

    fun onClickSendResetPasswordEmail(v: View) {

        val email = emailEditText.text.toString()

        if (isEmailValid(email)) {
            resetPasswordButton.text = getString(R.string.sending)

            Firebase.auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    resetPasswordButton.text = getString(R.string.sent)

                    Handler().postDelayed({
                        finish()
                    }, 1000)
                }
                .addOnFailureListener {
                    // TODO - Implement
                    resetPasswordButton.text = getString(R.string.failed)
                }
        }
    }

    private val formFormWatcher = object: TextWatcher {
        override fun afterTextChanged(p0: Editable?) { }
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val email = emailEditText.text.toString()
            resetPasswordButton.isEnabled = isEmailValid(email)
        }
    }

    fun onClickBack(v: View) {
        finish()
    }

    private fun isEmailValid(email: String): Boolean {
        // Email:
        //   - email pattern
        //   - min 4 characters
        //   - max 30 characters
        return email.length in 4..30 && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}