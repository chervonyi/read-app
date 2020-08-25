package room106.app.read.views

import android.content.Context
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import room106.app.read.R

class AdsImageView(context: Context?) : androidx.appcompat.widget.AppCompatImageView(context) {

    init {
        if (context != null) {
            background = ContextCompat.getDrawable(context, R.drawable.content_panel)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400)
            layoutParams = params
        }
    }
}