package org.sert2521.bunnybots.util

import edu.wpi.first.wpilibj.RobotController
import org.team2471.frc.lib.math.Point
import org.team2471.frc.lib.math.average
import kotlin.math.cos
import kotlin.math.sin

class PositionEstimator(var position: Point, private var heading: Double, time: Long = RobotController.getFPGATime()) {
    private var lastTimestamp = time

    /**
     * Updates the estimated [position] based on given values.
     *
     * @param leftVelocity The current left side drive train velocity in units/second.
     * @param rightVelocity The current right side drive train velocity in units/second.
     * @param heading The current continuous heading in degrees.
     * @param time The current time in milliseconds. Defaults to [RobotController.getFPGATime].
     */
    fun updatePosition(
        leftVelocity: Double,
        rightVelocity: Double,
        heading: Double,
        time: Long = RobotController.getFPGATime()
    ) {
        val dt = (time - lastTimestamp) / 1000000.0 // convert to seconds
        val avgVelocity = average(leftVelocity, rightVelocity)
        val deltaHeading = Math.toRadians(heading - this.heading) // convert to radians for trig functions

        println("$avgVelocity, ${cos(deltaHeading)}, $dt")
        position += Point(avgVelocity * sin(deltaHeading), avgVelocity * cos(deltaHeading)) * dt

        this.heading = heading
        lastTimestamp = time
    }

    fun reset(position: Point = Point.ORIGIN, heading: Double = this.heading) {
        this.position = position
        this.heading = heading
    }
}
