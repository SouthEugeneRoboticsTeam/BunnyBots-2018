package org.sert2521.bunnybots.util

/**
 * Returns the next value in an enum, or the first if the current value is the last.
 */
inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}
