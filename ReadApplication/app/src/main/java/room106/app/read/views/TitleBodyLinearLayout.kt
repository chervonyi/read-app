package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.*
import room106.app.read.R
import java.util.*
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

    private var appID = "ca-app-pub-3940256099942544/2247696110"
    private var adLoader: AdLoader? = null
    private val chapterLength = 500
    private var indexes: Queue<Int>? = null
    private var nativeAdsList = ArrayList<UnifiedNativeAd>()
    private var isDestroyed = false

    private fun initializeView(context: Context?) {
        orientation = VERTICAL
        if (context != null) {
            dividerDrawable = ContextCompat.getDrawable(context, R.drawable.divider_form)
            showDividers = SHOW_DIVIDER_MIDDLE
            adLoader = initializeAdLoader(context)
        }
    }

    fun setBodyText(body: String) {
        val regex = Pattern.compile("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?|\\!)\\s+")
        val sentences = body.split(regex)

        var chapter = ""
        indexes = LinkedList<Int>()
        var index = 1

        for (sentence in sentences) {
            Log.d("TitleBodyLinearLayout", "-$sentence\n")
            if (chapter.length >= chapterLength) {
                appendChapter(chapter)
                indexes!!.add(index)
                index += 2
                chapter = ""
            }

            chapter += sentence
        }
        appendChapter(chapter)
        loadAds()
    }

    private fun appendChapter(text: String) {
        if (context != null) {
            val textView = ChapterTextView(context)
            textView.text = text
            addView(textView)
        }
    }

    private fun loadAds() {
        val adsCount = childCount - 1
        Log.d("AdMob", "loadAds. childCount: $childCount. adsCount: $adsCount")
        adLoader!!.loadAds(AdRequest.Builder().build(), adsCount)
    }

    private fun initializeAdLoader(context: Context): AdLoader {
        return AdLoader.Builder(context, appID)
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->

                if (isDestroyed) {
                    ad.destroy()
                    return@forUnifiedNativeAd
                }

                nativeAdsList.add(ad)
                val adView = inflate(context, R.layout.ad_unified, null)
                        as UnifiedNativeAdView
                displayUnifiedNativeAd(ad, adView)
            }
            .build()
    }

    private fun displayUnifiedNativeAd(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        Log.d("AdMob", "displayUnifiedNativeAd!")

        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        headlineView.text = nativeAd.headline
        adView.headlineView = headlineView

        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        bodyView.text = nativeAd.body
        adView.bodyView = bodyView

        val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
        val icon: NativeAd.Image = nativeAd.icon
        iconView.setImageDrawable(icon.drawable)
        iconView.visibility = VISIBLE
        adView.iconView = iconView

        val starRatingView = adView.findViewById<RatingBar>(R.id.ad_stars)
        if (nativeAd.starRating == null) {
            starRatingView.visibility = INVISIBLE
        } else {
            starRatingView.rating = nativeAd.starRating.toFloat()
            starRatingView.visibility = VISIBLE
        }
        adView.starRatingView = starRatingView

        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
        if (nativeAd.advertiser == null) {
            advertiserView.visibility = INVISIBLE
        } else {
            advertiserView.text = nativeAd.advertiser
            advertiserView.visibility = VISIBLE
        }
        adView.advertiserView = advertiserView

        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
        if (nativeAd.mediaContent == null) {
            mediaView.visibility = INVISIBLE
        } else {
            mediaView.setMediaContent(nativeAd.mediaContent)
            mediaView.visibility = VISIBLE
        }
        adView.mediaView = mediaView

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd)

        // Add view in appropriate place between chapters
        val currentIndex = indexes?.remove()
        if (currentIndex != null) {
            addView(adView, currentIndex)
        }
    }

    fun destroyAds() {
        isDestroyed = true
        for (nativeAd in nativeAdsList) {
            Log.d("AdMob", "Destroy ad")
            nativeAd.destroy()
        }
    }
}