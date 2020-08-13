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
import room106.app.read.models.LikedTitle
import room106.app.read.models.Title

@SuppressLint("ViewConstructor")
class TitleView : LinearLayout {

    // Views
    private lateinit var headerTextView: TextView
    private lateinit var authorTextView: TextView
    private lateinit var authorAvatarImageView: RoundedImageView
    private lateinit var descriptionTextView: TextView

    constructor(context: Context?, title: Title, titleID: String): super(context) {
        initView(context, title.title, title.description, title.authorName, title.authorAvatar)
    }

    constructor(context: Context?, title: LikedTitle): super(context) {
        initView(context, title.title, title.description, title.authorName, title.authorAvatar)
    }

    constructor(context: Context?,
                title: String,
                description: String,
                authorName: String,
                authorAvatar: Int) : super(context) {
        initView(context, title, description, authorName, authorAvatar)
    }

    private fun initView(context: Context?,
                        title: String,
                         description: String,
                         authorName: String,
                         authorAvatar: Int) {
        View.inflate(context, R.layout.title_layout, this)

        // Connect views
        headerTextView = findViewById(R.id.titleHeaderTextView)
        authorTextView = findViewById(R.id.titleAuthorTextView)
        authorAvatarImageView = findViewById(R.id.titleAuthorAvatarImageView)
        descriptionTextView = findViewById(R.id.titleDescriptionTextView)

        // Assign data
        headerTextView.text = title
        authorTextView.text = authorName
        descriptionTextView.text = description

        val avatarName = "ic_avatar_${authorAvatar}"
        val image = resources.getIdentifier(avatarName, "drawable", context?.packageName)
        authorAvatarImageView.setImageResource(image)

    }
}