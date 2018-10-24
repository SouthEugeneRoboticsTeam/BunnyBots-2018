package org.sert2521.bunnybots.intake

import org.sertain.command.Subsystem
import org.sertain.hardware.Talon

object Intake : Subsystem() {
    const val INTAKE_SPEED = 0

    val motor = Talon(0)
}
