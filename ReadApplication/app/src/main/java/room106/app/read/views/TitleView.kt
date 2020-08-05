package room106.app.read.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.makeramen.roundedimageview.RoundedImageView
import room106.app.read.R
import room106.app.read.activities.TitleActivity
import room106.app.read.models.Title

@SuppressLint("ViewConstructor")
class TitleView(context: Context?, private val title: Title, private val titleID: String) : LinearLayout(context),
    View.OnClickListener {

    // Views
    private lateinit var header: TextView
    private lateinit var author: TextView
    private lateinit var authorAvatar: RoundedImageView
    private lateinit var description: TextView

    init {
        View.inflate(context, R.layout.title_layout, this)

        // Connect views
        header = findViewById(R.id.titleHeader)
        author = findViewById(R.id.titleAuthor)
        authorAvatar = findViewById(R.id.titleAuthorAvatar)
        description = findViewById(R.id.titleDescription)

        // Assign data
        header.text = title.title
        author.text = title.authorName
        description.text = title.description

        val avatarName = "ic_avatar_${title.authorAvatar}"
        val image = resources.getIdentifier(avatarName, "drawable", context?.packageName)
        authorAvatar.setImageResource(image)

        setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val intent = Intent(context, TitleActivity::class.java)
        // TODO - put titledID in extra
        context.startActivity(intent)
    }


}