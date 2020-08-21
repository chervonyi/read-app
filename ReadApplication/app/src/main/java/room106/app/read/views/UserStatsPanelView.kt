package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import room106.app.read.R

class UserStatsPanelView: LinearLayout {

    //region Views
    private lateinit var titlesCountSkeleton: LinearLayout
    private lateinit var followersCountSkeleton: LinearLayout
    private lateinit var likesCountSkeleton: LinearLayout
    private lateinit var titlesCountLinearLayout: LinearLayout
    private lateinit var followersCountLinearLayout: LinearLayout
    private lateinit var likesCountLinearLayout: LinearLayout
    private lateinit var titlesCountTextView: TextView
    private lateinit var followersCountTextView: TextView
    private lateinit var likesCountTextView: TextView
    //endregion

    constructor(context: Context?) : super(context) {
        initializeView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initializeView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initializeView(context)
    }

    private fun initializeView(context: Context?) {
        View.inflate(context, R.layout.user_stat_panel_layout, this)

        // Connect views
        titlesCountSkeleton =           findViewById(R.id.titlesCountSkeleton)
        followersCountSkeleton =        findViewById(R.id.followersCountSkeleton)
        likesCountSkeleton =            findViewById(R.id.likesCountSkeleton)
        titlesCountLinearLayout =       findViewById(R.id.titlesCountLinearLayout)
        followersCountLinearLayout =    findViewById(R.id.followersCountLinearLayout)
        likesCountLinearLayout =        findViewById(R.id.likesCountLinearLayout)
        titlesCountTextView =           findViewById(R.id.titlesCountTextView)
        followersCountTextView =        findViewById(R.id.followersCountTextView)
        likesCountTextView =            findViewById(R.id.likesCountTextView)
    }

    fun attachData(titlesCount: Int, followersCount: Int, likesCount: Int) {
        // Set data
        titlesCountTextView.text = titlesCount.toString()
        followersCountTextView.text = followersCount.toString()
        likesCountTextView.text = likesCount.toString()

        // Hide skeleton panels
        titlesCountSkeleton.visibility = View.GONE
        followersCountSkeleton.visibility = View.GONE
        likesCountSkeleton.visibility = View.GONE

        // Show data panels
        titlesCountLinearLayout.visibility = View.VISIBLE
        followersCountLinearLayout.visibility = View.VISIBLE
        likesCountLinearLayout.visibility = View.VISIBLE
    }
}