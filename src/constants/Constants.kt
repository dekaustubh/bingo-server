package com.dekaustubh.constants

import com.dekaustubh.models.User.User
import io.ktor.util.AttributeKey

object Request {
    const val TOKEN = "token"
}

object Response {
    const val USER = "user"
    val USER_ATTR = AttributeKey<User>(USER)
}

object Separator {
    const val COMMA = ","
}

object Db {
    const val LIMIT = 20
    const val OFFSET = 0
}