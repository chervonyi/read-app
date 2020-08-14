package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import room106.app.read.R

class PublishButton: androidx.appcompat.widget.AppCompatButton {
    constructor(context: Context?) : super(context) {
        initView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context?) {
        if (context != null) {
            background = ContextCompat.getDrawable(context, R.drawable.simple_button)
            setTextColor(ContextCompat.getColor(context, R.color.colorFontPublishButton))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.buttonFontSize))
            isAllCaps = false
            typeface = ResourcesCompat.getFont(context, R.font.cabin_semi_bold)
            isFocusable = false
            stateListAnimator = null
        }
    }
}