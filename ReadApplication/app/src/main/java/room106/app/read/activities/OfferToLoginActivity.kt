package room106.app.read.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import room106.app.read.R

class OfferToLoginActivity : AppCompatActivity() {

    private lateinit var toolBar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer_to_login)

        // Connect views
        toolBar = findViewById(R.id.toolBar)

        // Set Listeners
        toolBar.setNavigationOnClickListener(onClickBackListener)
    }

    fun onClickSignUp(v: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickLogin(v: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    private val onClickBackListener = View.OnClickListener {
        finish()
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
}