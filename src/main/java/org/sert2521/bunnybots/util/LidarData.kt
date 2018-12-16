package org.sert2521.bunnybots.util

object Lidar {
    var alive: Boolean = false
    var time: Long? = 0

    var distance: Double? = null

    var xOffset: Double? = null
    var yOffset: Double? = null
    var theta: Double? = null

    var xOffsets = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    val xOffsetAverage get() = xOffsets.average()

    var yOffsets = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    val yOffsetAverage get() = yOffsets.average()

    var thetas = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    val thetaAverage get() = thetas.average()
}

data class LidarData(
    val alive: Boolean? = null,
    val time: Long? = null,
    val d: Double? = null,
    val x: Double? = null,
    val y: Double? = null,
    val t: Double? = null
)
