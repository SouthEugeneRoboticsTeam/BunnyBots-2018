package org.sert2521.bunnybots.intake

import org.sert2521.bunnybots.intake.commands.IntakeControl
import org.sert2521.bunnybots.util.INTAKE_MOTOR
import org.sertain.command.Subsystem
import org.sertain.hardware.Talon

object Intake : Subsystem() {
    const val INTAKE_SPEED = 0.0

    val motor = Talon(INTAKE_MOTOR)

    override val defaultCommand get() = IntakeControl()
}
