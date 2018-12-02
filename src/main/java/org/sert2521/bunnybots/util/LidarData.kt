package org.sert2521.bunnybots.util

object Lidar {
    var alive: Boolean = false
    var time: Long? = 0
    var xOffset: Double? = null
    var yOffset: Double? = null
    var theta: Double? = null
}

data class LidarData(
    val alive: Boolean? = null,
    val time: Long? = null,
    val xOffset: Double? = null,
    val yOffset: Double? = null,
    val theta: Double? = null
)
