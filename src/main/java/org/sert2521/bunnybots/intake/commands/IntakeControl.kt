package org.sert2521.bunnybots.intake.commands

import org.sert2521.bunnybots.intake.Intake
import org.sertain.command.Command

class IntakeControl() : Command() {
    init {
        requires(Intake)
    }

    var shouldRun = false

    override fun execute(): Boolean {
        Intake.motor.set(if (shouldRun) Intake.INTAKE_SPEED else 0.0)
        if (Intake.switch.get()) shouldRun = !shouldRun
        return false
    }
}
