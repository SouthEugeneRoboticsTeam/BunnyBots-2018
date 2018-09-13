package org.sert2521.bunnybots.drivetrain.commands

import edu.wpi.first.wpilibj.GenericHID
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.util.driveSpeedScalar
import org.sert2521.bunnybots.util.controller
import org.sertain.command.Command

/**
 * Allows for teleoperated drive of the robot.
 */
class TeleopDrive : Command() {
    init {
        requires(Drivetrain)
    }

    override fun execute(): Boolean {
        Drivetrain.arcade(
                driveSpeedScalar * -controller.getY(GenericHID.Hand.kLeft),
                driveSpeedScalar * controller.getX(GenericHID.Hand.kRight)
        )

        return false
    }

    override fun onDestroy() = Drivetrain.stop()
}
