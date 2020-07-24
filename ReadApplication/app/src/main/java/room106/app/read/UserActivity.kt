package room106.app.read

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import room106.app.read.models.User

class UserActivity : AppCompatActivity() {

    // Views
    private lateinit var userAvatarImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userTitlesCountTextView: TextView
    private lateinit var userFollowersCountTextView: TextView
    private lateinit var userLikesCountTextView: TextView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Connect views
        userAvatarImageView = findViewById(R.id.imageViewUserAvatar)
        userNameTextView = findViewById(R.id.textViewUserName)
        userTitlesCountTextView = findViewById(R.id.textViewUserTitlesCount)
        userFollowersCountTextView = findViewById(R.id.textViewUserFollowersCount)
        userLikesCountTextView = findViewById(R.id.textViewUserLikesCount)

        auth = Firebase.auth
        db = Firebase.firestore

        // TODO - If extras contains some UID, then use this uid.
        // TODO - If extras does not contain any UID, then use Auth.currentUser.uid.

        if (auth.currentUser != null) {
            loadUserData(auth.currentUser!!.uid)
        }
    }

    private fun loadUserData(uid: String) {

        val userRef = db.collection("users").document(uid)

        // TODO - Assign realtime listener or read once
        userRef.get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)

                if (user != null) {

                    // TODO - Assign appropriate avatar
                    userNameTextView.text =             user.name
                    userTitlesCountTextView.text =      user.titlesCount.toString()
                    userFollowersCountTextView.text =   user.followersCount.toString()
                    userLikesCountTextView.text =       user.likesCount.toString()
                }
            }

            .addOnFailureListener {
                // TODO - Implement
            }
    }
}