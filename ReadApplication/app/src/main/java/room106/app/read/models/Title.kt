package room106.app.read.models

import com.google.firebase.Timestamp

data class Title(val title: String = "",
                 val description: String = "",
                 val authorName: String = "",
                 val authorID: String = "",
                 val authorAvatar: Int = 0,
                 val status: String = "",
                 val publicationTime: Timestamp = Timestamp.now(),
                 val lastTimeUpdated: Timestamp = Timestamp.now(),
                 val readsCount: Int = 0,
                 val likesCount: Int = 0)
