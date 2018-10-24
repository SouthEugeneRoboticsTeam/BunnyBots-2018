package org.sert2521.bunnybots.arm

import org.sert2521.bunnybots.util.ARM_MOTOR
import org.sertain.command.Subsystem
import org.sertain.hardware.Talon

object Arm : Subsystem() {
    private val armMotor = Talon(ARM_MOTOR)
}
