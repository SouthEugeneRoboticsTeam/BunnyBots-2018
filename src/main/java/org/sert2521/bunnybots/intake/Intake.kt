package org.sert2521.bunnybots.intake

import org.sert2521.bunnybots.intake.commands.IntakeControl
import org.sertain.command.Subsystem
import org.sertain.hardware.DigitalInput
import org.sertain.hardware.Talon

object Intake : Subsystem() {
    const val INTAKE_SPEED = 0.0

    val motor = Talon(0)
    val switch = DigitalInput(0)

    override fun onTeleopStart() {
        IntakeControl().start()
    }
}
