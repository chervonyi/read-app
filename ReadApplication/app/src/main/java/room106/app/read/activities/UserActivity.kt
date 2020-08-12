package room106.app.read.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.fragments.CurrentUserFragment
import room106.app.read.fragments.OtherUserFragment
import room106.app.read.models.User

class UserActivity : AppCompatActivity() {

    //region Fields
    // Views
    private lateinit var userAvatarImageView: RoundedImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userTitlesCountTextView: TextView
    private lateinit var userFollowersCountTextView: TextView
    private lateinit var userLikesCountTextView: TextView
    private lateinit var userTitlesFrameLayout: FrameLayout
    private lateinit var createNewTitleButton: Button
    private lateinit var followButton: Button

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userData: User
    private var userID: String? = null
    //endregion

    //region Start Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Connect views
        userAvatarImageView = findViewById(R.id.userAvatarImageView)
        userNameTextView = findViewById(R.id.userNameTextView)
        userTitlesCountTextView = findViewById(R.id.userTitlesCountTextView)
        userFollowersCountTextView = findViewById(R.id.userFollowersCountTextView)
        userLikesCountTextView = findViewById(R.id.userLikesCountTextView)
        userTitlesFrameLayout = findViewById(R.id.userTitlesFrameLayout)
        createNewTitleButton = findViewById(R.id.createNewTitleButton)
        followButton = findViewById(R.id.followButton)

        auth = Firebase.auth
        db = Firebase.firestore

        userID = intent.getStringExtra("user_id")
    }

    override fun onStart() {
        super.onStart()

        val ft = supportFragmentManager.beginTransaction()

        if (auth.currentUser != null && (userID == null || userID == auth.currentUser?.uid)) {
            // Load current user data
            readUserData(auth.currentUser!!.uid)

            createNewTitleButton.visibility = View.VISIBLE
            followButton.visibility = View.GONE

            // Set appropriate bottom panel
            ft.replace(R.id.userTitlesFrameLayout, CurrentUserFragment())
            ft.commit()
        } else if (userID != null) {
            // Load other user data
            readUserData(userID!!)

            createNewTitleButton.visibility = View.GONE
            followButton.visibility = View.VISIBLE

            // Set appropriate bottom panel
            ft.replace(R.id.userTitlesFrameLayout, OtherUserFragment(userID!!))
            ft.commit()
        } else {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        }

        checkIfFollowing()
    }
    //endregion

    //region User Data
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
    //endregion

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
        intent.putExtra("purpose", "change_avatar")
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    private fun logOutUser() {
        auth.signOut()

        // Go to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
    //endregion

    //region Click Listeners
    fun onClickCreateNewTitle(v: View) {
        val intent = Intent(this, EditTitleActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickBack(v: View) {
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
    //endregion

    //region Follow
    fun onClickFollow(v: View) {
        if (userID != null) {

            val currentUserID = auth.currentUser?.uid

            if (currentUserID == null) {
                // Offer to login
                val intent = Intent(this, OfferToLoginActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)

            } else if (followingDocID != null) {
                // User has already followed this account
                db.collection("following").document(followingDocID!!).delete()
                    .addOnSuccessListener {
                        followingDocID = null

                        // Decrement followersCount
                        db.collection("users").document(userID!!)
                            .update("followersCount", FieldValue.increment(-1))
                    }

            } else if (userID != currentUserID) {
                // Follow user
                val followDoc = hashMapOf(
                    "userID" to userID,
                    "followerID" to currentUserID
                )

                db.collection("following").add(followDoc)
                    .addOnSuccessListener {
                        followingDocID = it.id

                        // Increment followersCount
                        db.collection("users").document(userID!!)
                            .update("followersCount", FieldValue.increment(1))
                    }
            }
        }
    }

    private var _followingDocID: String? = null

    private var followingDocID: String?
        get() {
            return _followingDocID
        }
        set(newValue) {
            _followingDocID = newValue

            if (newValue != null) {
                followButton.setBackgroundResource(R.drawable.simple_button)
                followButton.text = getString(R.string.you_following)
                followButton.setTextColor(ContextCompat.getColor(this, R.color.colorSimpleButtonText))
            } else {
                followButton.setBackgroundResource(R.drawable.main_button)
                followButton.text = getString(R.string.follow)
                followButton.setTextColor(ContextCompat.getColor(this, R.color.colorFontMainButton))
            }
        }

    private fun checkIfFollowing() {
        val currentUserID = auth.currentUser?.uid

        if (userID != null && currentUserID != null && userID != currentUserID) {

            db.collection("following")
                .whereEqualTo("userID", userID)
                .whereEqualTo("followerID", currentUserID)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.documents.size > 0) {
                        followingDocID = documents.documents[0].id
                    }
                }
        } else {
            followingDocID = null
        }
    }
    //endregion
}