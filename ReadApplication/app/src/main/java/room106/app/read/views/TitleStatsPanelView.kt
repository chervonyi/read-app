package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import room106.app.read.R
import java.lang.Math.abs
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class TitleStatsPanelView: LinearLayout {

    //region Views

    private lateinit var readsCountSkeleton: LinearLayout
    private lateinit var likesCountSkeleton: LinearLayout
    private lateinit var timeToReadSkeleton: LinearLayout
    private lateinit var readsCountLinearLayout: LinearLayout
    private lateinit var likesCountLinearLayout: LinearLayout
    private lateinit var timeToReadLinearLayout: LinearLayout
    private lateinit var readsCountTextView: TextView
    private lateinit var likesCountTextView: TextView
    private lateinit var timeToReadTextView: TextView
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
        View.inflate(context, R.layout.title_stat_panel_layout, this)

        // Connect views
        readsCountSkeleton =            findViewById(R.id.readsCountSkeleton)
        likesCountSkeleton =            findViewById(R.id.likesCountSkeleton)
        timeToReadSkeleton =            findViewById(R.id.timeToReadSkeleton)
        readsCountLinearLayout =        findViewById(R.id.readsCountLinearLayout)
        likesCountLinearLayout =        findViewById(R.id.likesCountLinearLayout)
        timeToReadLinearLayout =        findViewById(R.id.timeToReadLinearLayout)
        readsCountTextView =            findViewById(R.id.readsCountTextView)
        likesCountTextView =            findViewById(R.id.likesCountTextView)
        timeToReadTextView =            findViewById(R.id.timeToReadTextView)
    }

    fun attachData(readsCount: Int, likesCount: Int, timeToRead: Int) {
        // Set data
        readsCountTextView.text = formatNumber(readsCount)
        likesCountTextView.text = formatNumber(likesCount)
        timeToReadTextView.text = timeToRead.toString()

        // Hide skeleton panels
        readsCountSkeleton.visibility = View.GONE
        likesCountSkeleton.visibility = View.GONE
        timeToReadSkeleton.visibility = View.GONE

        // Show data panels
        readsCountLinearLayout.visibility = View.VISIBLE
        likesCountLinearLayout.visibility = View.VISIBLE
        timeToReadLinearLayout.visibility = View.VISIBLE
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
}