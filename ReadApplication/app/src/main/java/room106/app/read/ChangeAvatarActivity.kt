package room106.app.read

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.models.User


class ChangeAvatarActivity : AppCompatActivity() {

    // Views
    private lateinit var avatarsLinearLayout: LinearLayout

    private var selectedAvatar = 0

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_avatar)

        // Connect views
        avatarsLinearLayout = findViewById(R.id.avatarsLinearLayout)

        // Firebase
        auth = Firebase.auth
        db = Firebase.firestore

        selectCurrentUserAvatar()
    }

    private fun selectCurrentUserAvatar() {

        if (intent.hasExtra("avatar_id")) {
            val avatarId = intent.getIntExtra("avatar_id", 0)
            highlightAvatarImageView(avatarId)
        } else {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userRef = db.collection("users").document(currentUser.uid)

                userRef.get().addOnSuccessListener { document ->
                    val userData = document.toObject(User::class.java)

                    if (userData?.avatar != null) {
                        highlightAvatarImageView(userData.avatar)
                    }
                }
            }
        }
    }

    // On Click avatar
    fun onSelectAvatar(v: View) {
        highlightAvatarImageView(v.tag.toString().toInt())
    }

    private fun highlightAvatarImageView(id: Int) {
        for (i in 0 until avatarsLinearLayout.childCount) {
            val innerList = avatarsLinearLayout.getChildAt(i) as LinearLayout
            for (j in 0 until innerList.childCount) {
                val avatarImageView = innerList.getChildAt(j) as RoundedImageView

                if (avatarImageView.tag.toString().toInt() == id) {
                    // Highlight
                    avatarImageView.borderColor = ContextCompat.getColor(this, R.color.colorAvatarBorder)

                    // Save selected avatar ID
                    selectedAvatar = avatarImageView.tag.toString().toInt()

                    // Save selected avatar in database
                    saveSelectedAvatar()
                } else {
                    // Remove highlight
                    avatarImageView.borderColor = ContextCompat.getColor(this, android.R.color.transparent)
                }
            }
        }
    }


    fun onClickBack(v: View) {
        finish()
    }

    fun onClickFinish(v: View) {
        // TODO - Implement
    }

    private fun saveSelectedAvatar() {

        val currentUser = auth.currentUser
        if (currentUser != null && selectedAvatar in 0..14) {

            val userRef = db.collection("users").document(currentUser.uid)
            userRef.update("avatar", selectedAvatar)
        }
    }

}