package org.sert2521.bunnybots.util

inline fun <reified T: Enum<T>> T.next(): T {
    return try {
        val values = enumValues<T>()
        val nextOrdinal = (ordinal + 1) % values.size
        values[nextOrdinal]
    } catch (e: Exception) {
        this
    }
}
