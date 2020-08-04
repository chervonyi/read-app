package room106.app.read.views

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import room106.app.read.R

class TitleView(context: Context?) : LinearLayout(context) {

    init {
        View.inflate(context, R.layout.title_layout, this)
    }
}