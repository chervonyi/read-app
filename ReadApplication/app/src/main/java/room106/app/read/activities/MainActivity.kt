package room106.app.read.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import room106.app.read.models.User

class MainActivity : AppCompatActivity() {

    // Views
    private lateinit var userAccountImageButton: RoundedImageView
    private lateinit var anonymousUserImageButton: ImageView
    private lateinit var titleTypesTabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Connect views
        userAccountImageButton = findViewById(R.id.userAccountImageButton)
        anonymousUserImageButton = findViewById(R.id.anonymousUserImageButton)
        titleTypesTabLayout = findViewById(R.id.titleTypesTabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Prepare tab
        titleTypesTabLayout.addOnTabSelectedListener(onTabSelectedListener)
        viewPager.adapter = TitleTypesFragmentPageAdapter(supportFragmentManager)
        viewPager.addOnPageChangeListener(onPageChangeListener)

        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onStart() {
        super.onStart()
        readUserData()
    }

    //region User data
    fun onClickAnonymousUser(v: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun onClickUserAccount(v: View) {
        val intent = Intent(this, UserActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
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

            // Set avatar
            val avatarName = "ic_avatar_${userData.avatar}"
            val image = resources.getIdentifier(avatarName, "drawable", packageName)
            userAccountImageButton.setImageResource(image)
            userAccountImageButton.visibility = View.VISIBLE
            anonymousUserImageButton.visibility = View.INVISIBLE

        } else {
            // User Logged Out
            userAccountImageButton.visibility = View.INVISIBLE
            anonymousUserImageButton.visibility = View.VISIBLE
        }
    }
    //endregion

    //region Tab Listeners
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(p0: TabLayout.Tab?) { }

        override fun onTabUnselected(p0: TabLayout.Tab?) { }

        override fun onTabSelected(p0: TabLayout.Tab?) {
            viewPager.currentItem = p0!!.position
        }
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) { }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) { }

        override fun onPageSelected(position: Int) {
            val tab = titleTypesTabLayout.getTabAt(position)
            tab?.select()
        }
    }
    //endregion
}