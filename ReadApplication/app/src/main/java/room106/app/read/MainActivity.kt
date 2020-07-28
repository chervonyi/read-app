package room106.app.read

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.models.User

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var accountDetailsTextView: TextView
    private lateinit var userAccountImageButton: RoundedImageView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect views
        accountDetailsTextView = findViewById(R.id.accountDetails)
        userAccountImageButton = findViewById(R.id.userAccountImageButton)

        auth = Firebase.auth
        db = Firebase.firestore

        readUserData()
    }


    //region User data
    fun onClickUserAccount(v: View) {
        if (auth.currentUser != null) {
            // User Logged In
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        } else {
            // User Logged Out
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun readUserData() {
        val user = auth.currentUser

        if (user != null) {
            val userRef = db.collection("users").document(user.uid)

            // Read once or assign realtime listener
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    // Data read successfully
                    val userData = document.toObject(User::class.java)
                    updateUserUI(user, userData)
                }
            }
        } else {
            updateUserUI(null, null)
        }
    }

    private fun updateUserUI(user: FirebaseUser?, userData: User?) {

        if (user != null && userData != null) {
            // User Logged In

            // Set avatar
            val avatarName = "ic_avatar_${userData.avatar}"
            val image = resources.getIdentifier(avatarName, "drawable", packageName)
            userAccountImageButton.setImageResource(image)


            val accountDetails = "uid: ${user.uid} " +
                    "\nEmail: {${user.email}} " +
                    "\nName: ${userData.name} " +
                    "\nAvatar: ${userData.avatar} " +
                    "\nisPaid: ${userData.isPaid} " +
                    "\nRegistration: ${userData.registration} " +
                    "\nTitles: ${userData.titlesCount} " +
                    "\nFollowers: ${userData.followersCount} " +
                    "\nLikes: ${userData.likesCount}"

            accountDetailsTextView.text = accountDetails
        } else {
            // User Logged Out
            accountDetailsTextView.text = "User Logged Out"
        }
    }
    //endregion
}