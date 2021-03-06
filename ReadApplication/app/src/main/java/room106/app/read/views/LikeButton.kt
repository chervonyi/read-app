package room106.app.read.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import androidx.core.content.ContextCompat
import room106.app.read.R

class LikeButton: androidx.appcompat.widget.AppCompatButton {
    constructor(context: Context) : super(context) {
        initView(context)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context?) {
        if (context != null) {
            background = ContextCompat.getDrawable(context, R.drawable.like_button)
            setTextColor(ContextCompat.getColor(context, R.color.colorFontMainButton))
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.resources.getDimension(R.dimen.buttonFontSize))
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            isFocusable = false
            stateListAnimator = null
            includeFontPadding = false
        }
    }

    private var _isLiked = false
    var isLiked: Boolean
        get() {
            return _isLiked
        }

        set(newValue) {
            _isLiked = newValue

            if (newValue) {
                background = ContextCompat.getDrawable(context, R.drawable.simple_button)
                setTextColor(ContextCompat.getColor(context, R.color.colorFontSimpleButton))
            } else {
                background = ContextCompat.getDrawable(context, R.drawable.like_button)
                setTextColor(ContextCompat.getColor(context, R.color.colorFontMainButton))
            }
        }
}