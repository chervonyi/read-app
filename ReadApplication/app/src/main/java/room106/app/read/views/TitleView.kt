package room106.app.read.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.activities.MainActivity
import room106.app.read.activities.TitleActivity
import room106.app.read.models.Title

@SuppressLint("ViewConstructor")
class TitleView(context: Context?, private val title: Title, private val titleID: String) : LinearLayout(context){

    // Views
    private var headerTextView: TextView
    private var authorTextView: TextView
    private var authorAvatarImageView: RoundedImageView
    private var descriptionTextView: TextView

    init {
        View.inflate(context, R.layout.title_layout, this)

        // Connect views
        headerTextView = findViewById(R.id.titleHeaderTextView)
        authorTextView = findViewById(R.id.titleAuthorTextView)
        authorAvatarImageView = findViewById(R.id.titleAuthorAvatarImageView)
        descriptionTextView = findViewById(R.id.titleDescriptionTextView)

        // Assign data
        headerTextView.text = title.title
        authorTextView.text = title.authorName
        descriptionTextView.text = title.description

        val avatarName = "ic_avatar_${title.authorAvatar}"
        val image = resources.getIdentifier(avatarName, "drawable", context?.packageName)
        authorAvatarImageView.setImageResource(image)
    }
}