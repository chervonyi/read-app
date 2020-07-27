package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import room106.app.read.R


class SelectAvatarImageButton(context: Context, attrs: AttributeSet): androidx.appcompat.widget.AppCompatImageButton(context, attrs) {

    init {
        // Load avatar image from activity_change_avatar.xml file
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SelectAvatarImageButton,
            0, 0).apply {

            try {
                setImageResource(getResourceId(R.styleable.SelectAvatarImageButton_avatar, R.drawable.ic_avatar_0))
            } finally {
                recycle()
            }
        }

        scaleType = ScaleType.CENTER_CROP
        background = ContextCompat.getDrawable(context, R.drawable.avatar_shape)
        clipToOutline = true
    }

}