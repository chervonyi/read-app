package room106.app.read.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.models.User

class ChangeAvatarActivity : AppCompatActivity() {

    // Views
    private lateinit var toolBar: Toolbar
    private lateinit var avatarsLinearLayout: LinearLayout

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_avatar)

        // Connect views
        toolBar = findViewById(R.id.toolBar)
        avatarsLinearLayout = findViewById(R.id.avatarsLinearLayout)

        // Connect listeners
        toolBar.setOnMenuItemClickListener(onClickSubmitListener)
        toolBar.setNavigationOnClickListener(onClickBackListener)

        val purpose = intent.getStringExtra("purpose")

        if (purpose == "sign_up") {
            // Activity opened after SignUp -> Hide "Back" button
            toolBar.navigationIcon = null
        } else if (purpose == "change_avatar") {
            // Activity opened after UserActivity -> Hide "Submit" button
            toolBar.menu.findItem(R.id.changeAvatarMenuSubmit).isVisible = false
        }

        // Firebase
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onStart() {
        super.onStart()
        highlightCurrentUserAvatar()
    }

    // On Click avatar
    fun onSelectAvatar(v: View) {
        val avatarId = (v.tag.toString().toInt())
        highlightAvatar(avatarId)

        // Save selected avatar in database
        saveAvatar(avatarId)
    }

    private fun highlightAvatar(id: Int) {
        for (i in 0 until avatarsLinearLayout.childCount) {
            val innerList = avatarsLinearLayout.getChildAt(i) as LinearLayout
            for (j in 0 until innerList.childCount) {
                val avatarImageView = innerList.getChildAt(j) as RoundedImageView

                avatarImageView.borderColor = if (avatarImageView.tag.toString().toInt() == id) {
                    // Highlight
                     ContextCompat.getColor(this,
                         R.color.colorAvatarBorder
                     )
                } else {
                    // Remove highlight
                     ContextCompat.getColor(this, android.R.color.transparent)
                }
            }
        }
    }

    private fun highlightCurrentUserAvatar() {

        if (intent.hasExtra("avatar_id")) {
            val avatarId = intent.getIntExtra("avatar_id", 0)
            highlightAvatar(avatarId)
        } else {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userRef = db.collection("users").document(currentUser.uid)

                userRef.get().addOnSuccessListener { document ->
                    val userData = document.toObject(User::class.java)

                    if (userData?.avatar != null) {
                        highlightAvatar(userData.avatar)
                    }
                }
            }
        }
    }

    private fun saveAvatar(selectedAvatar: Int) {
        val currentUser = auth.currentUser

        if (currentUser != null && selectedAvatar in 0..14) {
            val userRef = db.collection("users").document(currentUser.uid)
            userRef.update("avatar", selectedAvatar)
        }
    }

    //region Tool bar
    private val onClickBackListener = View.OnClickListener {
        finish()
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    private val onClickSubmitListener = Toolbar.OnMenuItemClickListener  {
        when (it.itemId) {
            R.id.changeAvatarMenuSubmit -> {
                onClickSubmit()
                true
            }

            else -> false
        }
    }

    private fun onClickSubmit() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }
    //endregion
}