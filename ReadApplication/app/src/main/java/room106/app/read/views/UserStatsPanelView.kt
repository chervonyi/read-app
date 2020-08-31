package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import room106.app.read.R
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

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
        titlesCountSkeleton = findViewById(R.id.titlesCountSkeleton)
        followersCountSkeleton = findViewById(R.id.followersCountSkeleton)
        likesCountSkeleton = findViewById(R.id.likesCountSkeleton)
        titlesCountLinearLayout = findViewById(R.id.titlesCountLinearLayout)
        followersCountLinearLayout = findViewById(R.id.followersCountLinearLayout)
        likesCountLinearLayout = findViewById(R.id.likesCountLinearLayout)
        titlesCountTextView = findViewById(R.id.titlesCountTextView)
        followersCountTextView = findViewById(R.id.followersCountTextView)
        likesCountTextView = findViewById(R.id.likesCountTextView)

        skeletonIsShown = true
    }

    fun attachData(titlesCount: Int, followersCount: Int, likesCount: Int) {
        // Set data
        titlesCountTextView.text = formatNumber(titlesCount)
        followersCountTextView.text = formatNumber(followersCount)
        likesCountTextView.text = formatNumber(likesCount)

        skeletonIsShown = false
    }

    private fun formatNumber(number: Int): String {
        // Replaces 18271 with "18.2k"

        val suffix = charArrayOf(' ', 'k', 'm', 'b')
        val value = floor(log10(number.toDouble())).toInt()
        val base = value / 3

        return if (value >= 3 && base < suffix.size) {
            DecimalFormat("##.#").format(number / 10.0.pow(base * 3.toDouble())) +
                    suffix[base]
        } else {
            DecimalFormat("#,##0").format(number)
        }
    }

    private var _skeletonIsShown = false
    var skeletonIsShown: Boolean
        get() = _skeletonIsShown
        set(value) {

            if (value) {
                manageVisibility(View.VISIBLE, View.GONE)
            } else {
                manageVisibility(View.GONE, View.VISIBLE)
            }
        }

    private fun manageVisibility(skeletonPanelsVisibility: Int, dataPanelsVisibility: Int) {
        // Skeleton panels
        titlesCountSkeleton.visibility =    skeletonPanelsVisibility
        followersCountSkeleton.visibility = skeletonPanelsVisibility
        likesCountSkeleton.visibility =     skeletonPanelsVisibility

        // Data panels
        titlesCountLinearLayout.visibility =    dataPanelsVisibility
        followersCountLinearLayout.visibility = dataPanelsVisibility
        likesCountLinearLayout.visibility =     dataPanelsVisibility
    }
}