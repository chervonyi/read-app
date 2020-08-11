package room106.app.read.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import room106.app.read.R

class OtherUserFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO - delete fragment_other_user_titles.xml
//        val v = inflater.inflate(R.layout.fragment_other_user_titles, container, false)

        val v = inflater.inflate(R.layout.fragment_titles_list, container, false)
        // TODO - Read titles
        return v
    }
}