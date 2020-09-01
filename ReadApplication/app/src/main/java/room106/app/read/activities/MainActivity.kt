package room106.app.read.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.fragments.MainTabsFragment
import room106.app.read.fragments.SearchFragment
import room106.app.read.models.User

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var userAccountImageButton: RoundedImageView
    private lateinit var anonymousUserImageButton: ImageView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Fragments
    private lateinit var mainTabsFragment: MainTabsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this) {}

        // Connect views
        userAccountImageButton = findViewById(R.id.userAccountImageButton)
        anonymousUserImageButton = findViewById(R.id.anonymousUserImageButton)

        // Prepare fragments
        mainTabsFragment = MainTabsFragment()

        auth = Firebase.auth
        db = Firebase.firestore

        // Set MainTabsFragment on start
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frameLayout, mainTabsFragment)
        ft.commit()
    }

    override fun onStart() {
        super.onStart()
        readUserData()
    }

    //region User data
    fun onClickAnonymousUser(v: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    fun onClickUserAccount(v: View) {
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
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

        mainTabsFragment.isUserLoggedIn = user != null
    }

    private fun updateUserUI(user: FirebaseUser?, userData: User?) {
        if (user != null && userData != null) {
            // User Logged In

            // Set avatar
            val avatarName = "ic_avatar_${userData.avatar}"
            val image = resources.getIdentifier(avatarName, "drawable", packageName)
            userAccountImageButton.setImageResource(image)

            userAccountImageButton.visibility = View.VISIBLE
            anonymousUserImageButton.visibility = View.GONE
        } else {
            // User Logged Out
            userAccountImageButton.visibility = View.GONE
            anonymousUserImageButton.visibility = View.VISIBLE
        }
    }
    //endregion

    /*
    // SEARCH
    private val onClickSearchListener = View.OnClickListener {
        // Show SearchFragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frameLayout, searchFragment)
        ft.commit()

        isSearchActive = true

        // Hide header buttons
        anonymousUserImageButton.visibility = View.INVISIBLE
        userAccountImageButton.visibility = View.INVISIBLE

        val searchViewPadding = resources.getDimensionPixelOffset(R.dimen.searchViewPadding)
        val headerPadding = resources.getDimensionPixelOffset(R.dimen.headerPadding)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(searchViewPadding, headerPadding, searchViewPadding, 0)
        searchView.layoutParams = params
        searchView.background = ContextCompat.getDrawable(this, R.drawable.edit_text_background)

    }

    private val onClickCloseSearchListener = SearchView.OnCloseListener {
        // Show MainTabsFragment
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frameLayout, mainTabsFragment)
        ft.commit()

        isSearchActive = false

        if (userIsLoggedIn) {
            userAccountImageButton.visibility = View.VISIBLE
        } else {
            anonymousUserImageButton.visibility = View.VISIBLE
        }

        val headerPadding = resources.getDimensionPixelOffset(R.dimen.headerPadding)
        val params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(headerPadding, headerPadding, headerPadding, 0)
        searchView.layoutParams = params
        searchView.background = ContextCompat.getDrawable(this, android.R.color.transparent)

        false
    }

    private val onQueryTextListener = object: SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean { return false }

        override fun onQueryTextChange(newText: String?): Boolean {

            if (newText != null) {
                searchFragment.updateTitlesList(newText)
            }
            return false
        }
    }
     */
}