package room106.app.read

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.models.User

class UserActivity : AppCompatActivity() {

    // Views
    private lateinit var userAvatarImageView: RoundedImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userTitlesCountTextView: TextView
    private lateinit var userFollowersCountTextView: TextView
    private lateinit var userLikesCountTextView: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userData: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Connect views
        userAvatarImageView = findViewById(R.id.userAvatarImageView)
        userNameTextView = findViewById(R.id.userNameTextView)
        userTitlesCountTextView = findViewById(R.id.userTitlesCountTextView)
        userFollowersCountTextView = findViewById(R.id.userFollowersCountTextView)
        userLikesCountTextView = findViewById(R.id.userLikesCountTextView)

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onStart() {
        super.onStart()

        // TODO - If extras contains some UID, then use this uid.
        // TODO - If extras does not contain any UID, then use Auth.currentUser.uid.

        if (auth.currentUser != null) {
            readUserData(auth.currentUser!!.uid)
        }
    }

    private fun readUserData(uid: String) {
        val userRef = db.collection("users").document(uid)

        // Read once user data from database
        userRef.get().addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)

                if (user != null) {
                    userData = user
                    updateUserUI(user)
                }
            }

            .addOnFailureListener {
                // TODO - Implement
            }
    }

    private fun updateUserUI(userData: User) {
        // Set User avatar
        val avatarName = "ic_avatar_${userData.avatar}"
        val image = resources.getIdentifier(avatarName, "drawable", packageName)
        userAvatarImageView.setImageResource(image)

        // Set other user info
        userNameTextView.text =             userData.name
        userTitlesCountTextView.text =      userData.titlesCount.toString()
        userFollowersCountTextView.text =   userData.followersCount.toString()
        userLikesCountTextView.text =       userData.likesCount.toString()
    }

    //region Menu
    fun onClickShowUserMenu(v: View) {
        val menu = PopupMenu(this, v)

        menu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.currentUserMenuChangeAvatar -> {
                    changeAvatar()
                    true
                }

                R.id.currentUserMenuLogOut -> {
                    logOutUser()
                    true
                }

                else -> false
            }
        }
        menu.inflate(R.menu.current_user_menu)
        menu.show()
    }

    private fun changeAvatar() {
        // Go to ChangeAvatarActivity
        val intent = Intent(this, ChangeAvatarActivity::class.java)
        intent.putExtra("avatar_id", userData.avatar)
        startActivity(intent)
    }

    private fun logOutUser() {
        auth.signOut()

        // Go to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    //endregion

    fun onClickBack(v: View) {
        finish()
    }
}