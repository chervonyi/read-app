package room106.app.read.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.TitleTypesFragmentPageAdapter
import room106.app.read.fragments.CurrentUserFragment
import room106.app.read.fragments.MainTabsFragment
import room106.app.read.fragments.SearchFragment
import room106.app.read.models.User

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var searchView: SearchView
    private lateinit var userAccountImageButton: RoundedImageView
    private lateinit var anonymousUserImageButton: ImageView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Fragments
    private lateinit var mainTabsFragment: MainTabsFragment
    private lateinit var searchFragment: SearchFragment

    private var userIsLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect views
        searchView = findViewById(R.id.searchView)
        userAccountImageButton = findViewById(R.id.userAccountImageButton)
        anonymousUserImageButton = findViewById(R.id.anonymousUserImageButton)

        // Prepare fragments
        mainTabsFragment = MainTabsFragment()
        searchFragment = SearchFragment()

        auth = Firebase.auth
        db = Firebase.firestore

        // Set MainTabsFragment on start
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frameLayout, mainTabsFragment)
        ft.commit()

        // Attach listeners
        searchView.setOnSearchClickListener(onClickSearchListener)
        searchView.setOnCloseListener(onClickCloseSearchListener)
    }

    override fun onStart() {
        super.onStart()
        readUserData()
    }

    //region User data
    fun onClickAnonymousUser(v: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickUserAccount(v: View) {
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    private fun readUserData() {
        val user = auth.currentUser

        if (user != null) {
            val userRef = db.collection("users").document(user.uid)

            // Read once user data from database
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
            userIsLoggedIn = true

            // Set avatar
            val avatarName = "ic_avatar_${userData.avatar}"
            val image = resources.getIdentifier(avatarName, "drawable", packageName)
            userAccountImageButton.setImageResource(image)
            userAccountImageButton.visibility = View.VISIBLE
            anonymousUserImageButton.visibility = View.INVISIBLE

        } else {
            // User Logged Out
            userIsLoggedIn = false

            userAccountImageButton.visibility = View.INVISIBLE
            anonymousUserImageButton.visibility = View.VISIBLE
        }
    }
    //endregion

    private val onClickSearchListener = View.OnClickListener {
        // Show SearchFragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frameLayout, searchFragment)
        ft.commit()

        // Hide header buttons
        anonymousUserImageButton.visibility = View.INVISIBLE
        userAccountImageButton.visibility = View.INVISIBLE
    }

    private val onClickCloseSearchListener = SearchView.OnCloseListener {
        // Show MainTabsFragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frameLayout, mainTabsFragment)
        ft.commit()

        if (userIsLoggedIn) {
            userAccountImageButton.visibility = View.VISIBLE
        } else {
            anonymousUserImageButton.visibility = View.VISIBLE
        }

        false
    }
}