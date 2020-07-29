package room106.app.read

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
    }

    fun onClickClose(v: View) {
        finish()
    }
}