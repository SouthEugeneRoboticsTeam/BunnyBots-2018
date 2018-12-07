package org.sert2521.bunnybots.drivetrain

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
        Drivetrain.arcade(driveSpeedScalar * primaryJoystick.x,
                          -driveSpeedScalar * primaryJoystick.y)
    }
}

suspend fun followPath(path: Path2D) = use(Drivetrain) {
    try {
        Drivetrain.driveAlongPath(path)
    } finally {
        teleopDrive()
    }
}
