package room106.app.read.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
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

    // AdMob
    // TODO - Replace test appID with real appID
    private var appID = "ca-app-pub-3940256099942544/2247696110"
    private var adLoader: AdLoader? = null
    private var isDestroyed = false

    // Ads
    private var nativeAdsContainers = LinkedList<NativeAdContainerView>()
    private var nativeAdsList = ArrayList<UnifiedNativeAd>()

    private val chapterLength = 500
    private fun initializeView(context: Context?) {
        orientation = VERTICAL
        if (context != null) {
            dividerDrawable = ContextCompat.getDrawable(context, R.drawable.divider_form)
            showDividers = SHOW_DIVIDER_MIDDLE
            adLoader = initializeAdLoader(context)
        }
    }

    fun setBodyText(body: String, isPublished: Boolean) {
        val regex = Pattern.compile("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?|\\!)\\s+")
        val sentences = body.split(regex)

        var chapter = ""

        for (sentence in sentences) {
            if (chapter.length >= chapterLength) {
                appendChapter(chapter)

                if (isPublished) {
                    appendAdContainer()
                }
                chapter = ""
            }
            chapter += sentence
        }
        appendChapter(chapter)

        if (isPublished) {
            loadAds()
        }
    }

    private fun appendChapter(text: String) {
        if (context != null) {
            val textView = ChapterTextView(context)
            textView.text = text
            addView(textView)
        }
    }

    private fun appendAdContainer() {
        val adContainer = NativeAdContainerView(context)
        nativeAdsContainers.add(adContainer)
        addView(adContainer)
    }

    private fun loadAds() {
        Log.d("AdMob", "loadAds. childCount: $childCount. adsCount: ${nativeAdsContainers.size}")
        adLoader!!.loadAds(AdRequest.Builder().build(), nativeAdsContainers.size)
    }

    private fun initializeAdLoader(context: Context): AdLoader {
        return AdLoader.Builder(context, appID)
            .forUnifiedNativeAd { ad: UnifiedNativeAd ->
                Log.d("AdMob", "forUnifiedNativeAd")
                if (isDestroyed) {
                    ad.destroy()
                    return@forUnifiedNativeAd
                }

                nativeAdsList.add(ad)
                val adView = inflate(context, R.layout.ad_unified, null)
                        as UnifiedNativeAdView
                displayUnifiedNativeAd(ad, adView)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Failed to load ads -> remove Ads Containers
                    Log.d("AdMob", "ERROR: ${adError.code}")
                    removeEmptyContainer()
                }
            })
            .build()
    }

    private fun displayUnifiedNativeAd(nativeAd: UnifiedNativeAd, adView: UnifiedNativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        if (nativeAd.headline == null) {
            headlineView.visibility = INVISIBLE
        } else {
            headlineView.text = nativeAd.headline
            headlineView.visibility = VISIBLE
        }
        adView.headlineView = headlineView

        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        if (nativeAd.body == null) {
            bodyView.visibility = INVISIBLE
        } else {
            bodyView.text = nativeAd.body
            bodyView.visibility = VISIBLE
        }
        adView.bodyView = bodyView

        val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
        if (nativeAd.icon == null) {
            iconView.visibility = INVISIBLE
        } else {
            val icon: NativeAd.Image = nativeAd.icon
            iconView.setImageDrawable(icon.drawable)
            iconView.visibility = VISIBLE
        }
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

        // Add view in appropriate placeholder between chapters
        try {
            val currentContainer = nativeAdsContainers.remove()
            currentContainer.removeSkeleton()
            currentContainer.addView(adView)
        } catch (exp: NoSuchElementException) {}
    }

    fun destroyAds() {
        isDestroyed = true
        for (nativeAd in nativeAdsList) {
            Log.d("AdMob", "Destroy ad")
            nativeAd.destroy()
        }
    }

    private fun removeEmptyContainer() {
        try {
            while (nativeAdsContainers.size > 0) {
                Log.d("AdMob", "Remove empty ad container")
                val container = nativeAdsContainers.remove()
                removeView(container)
            }
        } catch (exp: NoSuchElementException) {}
    }
}