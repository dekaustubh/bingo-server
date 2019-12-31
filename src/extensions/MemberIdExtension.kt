package com.dekaustubh.extensions

import com.dekaustubh.constants.Separator

/**
 * Extension function to convert list of Long to Comma separated string.
 */
fun List<Long>.toStringPlayers(): String {
    val builder = StringBuilder()
    forEachIndexed { index, id ->
        builder.append(id)
        if (index < size - 1) builder.append(Separator.COMMA)
    }
    return builder.toString()
}

/**
 * Extension function to convert string with comma separated values to list of Long.
 */
fun String.toPlayers(): MutableList<Long> {
    val list = split(Separator.COMMA)
    val userIds = mutableListOf<Long>()
    list.forEach { userIds.add(it.toLong()) }
    return userIds
}