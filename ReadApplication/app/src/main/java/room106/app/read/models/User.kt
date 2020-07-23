package room106.app.read.models

import com.google.firebase.Timestamp

data class User( val name: String = "",
        val avatar: Int = 0,
        val isPaid: Boolean = false,
        val registration: Timestamp = Timestamp.now(),
        val articlesCount: Int = 0,
        val followersCount: Int = 0,
        val likesCount: Int = 0)
