package org.sert2521.bunnybots.drivetrain

import org.sert2521.bunnybots.util.Lidar
import org.sert2521.bunnybots.util.driveSpeedScalar
import org.sert2521.bunnybots.util.primaryJoystick
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion_profiling.Path2D

/**
 * Allows for teleoperated driveRaw of the robot.
 */
suspend fun teleopDrive() = use(Drivetrain) {
    periodic {
        Drivetrain.arcade(driveSpeedScalar * primaryJoystick.x, -driveSpeedScalar * primaryJoystick.y)
    }
}

suspend fun followPath(path: Path2D, extraTime: Double = 0.0, useLidar: Boolean = false) = use(Drivetrain) {
    Drivetrain.driveAlongPath(path, extraTime, useLidar)
}

suspend fun driveParallelToCrates() = use(Drivetrain) {
    val setpointDistance = 6.0 / 12.0
    val setpointAngle = 0.0

    val baseSpeed = 0.3
    val distMultiplier = 0.2
    val angleMultiplier = 0.01

    try {
        periodic {
            val distance = Lidar.distance ?: 0.0

            var leftSpeed = baseSpeed
            var rightSpeed = baseSpeed

            val distanceDiff = distance - setpointDistance
            val angleDiff = Drivetrain.ahrs.angle - setpointAngle

            val turnFactor = distanceDiff * distMultiplier + angleDiff * angleMultiplier

            leftSpeed -= turnFactor
            rightSpeed += turnFactor

            println("Speed($leftSpeed, $rightSpeed), Turn: $turnFactor, Dist Diff: $distanceDiff, Ang Diff: $angleDiff")

            Drivetrain.driveRaw(leftSpeed, rightSpeed)
        }
    } catch (e: Throwable) {
        Drivetrain.stop()
    }
}
