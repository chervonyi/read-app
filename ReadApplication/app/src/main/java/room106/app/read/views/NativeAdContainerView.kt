package room106.app.read.views

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import room106.app.read.R

class NativeAdContainerView(context: Context) : FrameLayout(context) {

    init {
        background = ContextCompat.getDrawable(context, R.drawable.content_panel)
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams = params

        // Add empty child with some height to show skeleton screen
        val child = View(context)
        val childParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500)
        child.layoutParams = childParams
        addView(child)
    }

    fun removeSkeleton() {
        removeAllViews()
        requestLayout()
    }
}