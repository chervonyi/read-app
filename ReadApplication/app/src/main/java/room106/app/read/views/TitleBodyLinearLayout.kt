package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import room106.app.read.R
import java.util.regex.Pattern

class TitleBodyLinearLayout: LinearLayout {

    //region Constructors
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
    //endregion

    private val chapterLength = 1300

    private fun initializeView(context: Context?) {
        orientation = VERTICAL
        if (context != null) {
            dividerDrawable = ContextCompat.getDrawable(context, R.drawable.divider_form)
            showDividers = SHOW_DIVIDER_MIDDLE
        }
    }

    fun setBodyText(body: String) {
        val regex = Pattern.compile("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?|\\!)\\s+")
        val sentences = body.split(regex)

        var chapter = ""
        for (sentence in sentences) {
            Log.d("TitleBodyLinearLayout", "-$sentence\n")
            if (chapter.length >= chapterLength) {
                appendChapter(chapter)
                appendAds()
                chapter = ""
            }

            chapter += sentence
        }
        appendChapter(chapter)
    }

    private fun appendChapter(text: String) {
        if (context != null) {
            Log.d("TitleBodyLinearLayout", "CHAPTER!")
            val textView = ChapterTextView(context)
            textView.text = text
            addView(textView)
        }
    }

    private fun appendAds() {
        if (context != null) {
            Log.d("TitleBodyLinearLayout", "ADS!")
            val adsView = AdsImageView(context)
            addView(adsView)
        }
    }
}