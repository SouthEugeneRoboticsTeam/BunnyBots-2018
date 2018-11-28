package org.sert2521.bunnybots.outtake

import org.sert2521.bunnybots.outtake.commands.OuttakeControl
import org.sert2521.bunnybots.util.OUTTAKE_BELT_MOTOR
import org.sert2521.bunnybots.util.OUTTAKE_FLAP_MOTOR
import org.sert2521.bunnybots.util.OUTTAKE_MOTOR
import org.sertain.command.Subsystem
import org.sertain.hardware.Talon

object Outtake : Subsystem() {
    const val OUTTAKE_SPEED = 0.0

    val beltMotor = Talon(OUTTAKE_BELT_MOTOR)
    val flapMotor = Talon(OUTTAKE_FLAP_MOTOR)

    override val defaultCommand get() = OuttakeControl()
}