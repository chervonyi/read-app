package room106.app.read.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import room106.app.read.R

class OfferToLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer_to_login)
    }

    fun onClickSignUp(v: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickLogin(v: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickBack(v: View) {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
}