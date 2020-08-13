package room106.app.read.models

import com.google.firebase.Timestamp

data class LikedTitle(val title: String = "",
                      val titleID: String = "",
                      val userID: String = "",
                      val description: String = "",
                      val authorName: String = "",
                      val authorID: String = "",
                      val authorAvatar: Int = 0,
                      val time: Timestamp = Timestamp.now())
