package room106.app.read.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import room106.app.read.R
import room106.app.read.views.TitleView

class NewFragment: Fragment() {

    // Views
    private lateinit var titlesLinearLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_new, container, false)

        // Connect Views
        titlesLinearLayout = v.findViewById(R.id.titlesLinearLayout)

        loadTitles()

        return v
    }

    private fun loadTitles() {
        // TODO - Implement

        val t1 = TitleView(context)
        val t2 = TitleView(context)
        val t3 = TitleView(context)
        val t4 = TitleView(context)
        val t5 = TitleView(context)
        val t6 = TitleView(context)

        titlesLinearLayout.addView(t1)
        titlesLinearLayout.addView(t2)
        titlesLinearLayout.addView(t3)
        titlesLinearLayout.addView(t4)
        titlesLinearLayout.addView(t5)
        titlesLinearLayout.addView(t6)
    }
}
