package org.sert2521.bunnybots.util

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.PeriodicScope
import org.team2471.frc.lib.coroutines.periodic

/**
 * Returns the next value in an enum, or the first if the current value is the last.
 */
inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

suspend inline fun timedPeriodic(period: Double = 0.02, watchOverrun: Boolean = true, time: Double, body: PeriodicScope.() -> Unit) {
    val timer = Timer().apply { start() }

    periodic(period, watchOverrun) {
        body(this)

        if (timer.get() >= time) exitPeriodic()
    }

    timer.stop()
}
