package room106.app.read.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.android.synthetic.main.activity_change_name.*
import room106.app.read.R
import room106.app.read.fragments.CurrentUserFragment
import room106.app.read.fragments.OtherUserFragment
import room106.app.read.models.User
import room106.app.read.views.MainButton
import room106.app.read.views.UserStatsPanelView

class UserActivity : AppCompatActivity() {

    //region Fields
    // Views
    private lateinit var toolBar: Toolbar
    private lateinit var userAvatarImageView: RoundedImageView
    private lateinit var userNameSkeleton: View
    private lateinit var userNameTextView: TextView
    private lateinit var userStatsPanel: UserStatsPanelView
    private lateinit var userTitlesFrameLayout: FrameLayout
    private lateinit var createNewTitleButton: Button
    private lateinit var followButton: MainButton

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var userData: User? = null
    private var userID: String? = null
    //endregion

    //region Start Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Connect views
        toolBar = findViewById(R.id.toolBar)
        userAvatarImageView = findViewById(R.id.userAvatarImageView)
        userNameSkeleton = findViewById(R.id.userNameSkeleton)
        userNameTextView = findViewById(R.id.userNameTextView)
        userTitlesFrameLayout = findViewById(R.id.userTitlesFrameLayout)
        createNewTitleButton = findViewById(R.id.createNewTitleButton)
        followButton = findViewById(R.id.followButton)
        userStatsPanel = findViewById(R.id.userStatsPanel)

        // Connect listeners
        toolBar.setOnMenuItemClickListener(onClickMenuListener)
        toolBar.setNavigationOnClickListener(onClickBackListener)

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

                    Handler().postDelayed({
                        updateUserUI(user)
                    }, 3000)

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
        userNameTextView.text = userData.name
        userNameTextView.visibility = View.VISIBLE
        userNameSkeleton.visibility = View.GONE
        userStatsPanel.attachData(userData.titlesCount, userData.followersCount, userData.likesCount)
    }
    //endregion

    //region Toolbar
    private val onClickMenuListener = Toolbar.OnMenuItemClickListener {
        when (it.itemId) {
            R.id.currentUserMenuChangeAvatar -> {
                onClickChangeAvatar()
                true
            }

            R.id.currentUserMenuChangeName -> {
                onClickChangeName()
                true
            }

            R.id.currentUserMenuChangePassword -> {
                onClickChangePassword()
                true
            }

            R.id.currentUserMenuLogOut -> {
                logOutUser()
                true
            }

            else -> false
        }
    }

    private fun onClickChangeAvatar() {
        // Go to ChangeAvatarActivity
        val intent = Intent(this, ChangeAvatarActivity::class.java)
        intent.putExtra("avatar_id", userData?.avatar)
        intent.putExtra("purpose", "change_avatar")
        startActivity(intent)
    }

    private fun onClickChangeName() {
        val intent = Intent(this, ChangeNameActivity::class.java)
        intent.putExtra("name", userData?.name)
        startActivity(intent)
    }

    private fun onClickChangePassword() {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    private fun logOutUser() {
        auth.signOut()

        // Go to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    private val onClickBackListener = View.OnClickListener {
        finish()
//        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }
    //endregion

    fun onClickCreateNewTitle(v: View) {
        val intent = Intent(this, EditTitleActivity::class.java)
        startActivity(intent)
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    //region Follow
    fun onClickFollow(v: View) {
        if (userID != null) {

            val currentUserID = auth.currentUser?.uid

            if (currentUserID == null) {
                // Offer to login
                val intent = Intent(this, OfferToLoginActivity::class.java)
                startActivity(intent)
                finish()
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)

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
                followButton.isFunctionActive = false
                followButton.text = getString(R.string.you_following)
            } else {
                followButton.isFunctionActive = true
                followButton.text = getString(R.string.follow)
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