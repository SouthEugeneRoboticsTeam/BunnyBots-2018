package org.sert2521.bunnybots.intake.commands

import org.sert2521.bunnybots.intake.Intake
import org.sert2521.bunnybots.util.secondaryJoystick
import org.sertain.command.Command

class IntakeControl : Command() {
    init {
        requires(Intake)
    }

    private val shouldToggle get() = secondaryJoystick.getRawButton(-1)
    private var shouldRun = false

    override fun execute(): Boolean {
        Intake.motor.set(if (shouldRun) Intake.INTAKE_SPEED else 0.0)
        if (shouldToggle) shouldRun = !shouldRun
        return false
    }
}
