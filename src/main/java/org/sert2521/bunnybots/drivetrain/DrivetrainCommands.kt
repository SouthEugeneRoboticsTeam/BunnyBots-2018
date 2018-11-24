package org.sert2521.bunnybots.drivetrain

import edu.wpi.first.wpilibj.GenericHID
import org.sert2521.bunnybots.util.controller
import org.sert2521.bunnybots.util.driveSpeedScalar
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use

/**
 * Allows for teleoperated driveRaw of the robot.
 */
suspend fun teleopDrive(subsystem: Subsystem? = null) = use(subsystem ?: Drivetrain) {
    Drivetrain.arcade(
            driveSpeedScalar * -controller.getY(GenericHID.Hand.kLeft),
            driveSpeedScalar * controller.getX(GenericHID.Hand.kRight)
    )
}
