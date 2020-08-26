package room106.app.read.views

import android.content.Context
import android.util.TypedValue
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import room106.app.read.R

class ChapterTextView(context: Context) : androidx.appcompat.widget.AppCompatTextView(context) {

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.titleBodyFontSize))
        typeface = ResourcesCompat.getFont(context, R.font.cabin_regular)
        setTextColor(ContextCompat.getColor(context, R.color.colorFontBody))
    }
}