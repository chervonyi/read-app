package room106.app.read

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.makeramen.roundedimageview.RoundedImageView


class ChangeAvatarActivity : AppCompatActivity(), View.OnClickListener {

    // Views
    private lateinit var avatarsLinearLayout: LinearLayout

    private var selectedAvatar = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_avatar)

        // Connect views
        avatarsLinearLayout = findViewById(R.id.avatarsLinearLayout)

        // Set listeners
        for (i in 0 until avatarsLinearLayout.childCount) {
            val innerList = avatarsLinearLayout.getChildAt(i) as LinearLayout
            for (j in 0 until innerList.childCount) {
                val avatarImageView = innerList.getChildAt(j) as RoundedImageView
                avatarImageView.setOnClickListener(this)
            }
        }
    }

    // On Click avatar
    override fun onClick(p0: View?) {

        for (i in 0 until avatarsLinearLayout.childCount) {
            val innerList = avatarsLinearLayout.getChildAt(i) as LinearLayout
            for (j in 0 until innerList.childCount) {
                val avatarImageView = innerList.getChildAt(j) as RoundedImageView

                if (avatarImageView == p0) {
                    // Highlight
                    avatarImageView.borderColor = ContextCompat.getColor(this, R.color.colorAvatarBorder)

                    // Save selected avatar ID
                    val fullName = resources.getResourceName(avatarImageView.id)
                    val name = fullName.substring(fullName.lastIndexOf("_") + 1)
                    selectedAvatar = name.toInt()
                } else {
                    // Remove highlight
                    avatarImageView.borderColor = ContextCompat.getColor(this, android.R.color.transparent)
                }
            }
        }
    }

}