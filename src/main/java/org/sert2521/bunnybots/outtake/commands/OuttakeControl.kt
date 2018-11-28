package org.sert2521.bunnybots.outtake.commands

import org.sert2521.bunnybots.outtake.Outtake
import org.sert2521.bunnybots.util.OUTTAKE_BELT_MOTOR
import org.sert2521.bunnybots.util.secondaryJoystick
import org.sertain.command.Command

class OuttakeControl : Command() {
    init {
        requires(Outtake)
    }

    //private val shouldToggle get() = secondaryJoystick.getRawButton(-1)
a
    private var shouldRun = false

    override fun execute(): Boolean {
        /*Outtake.motor.set(if (shouldRun) Outtake.OUTTAKE_SPEED else 0.0)
        if (shouldToggle) shouldRun = !shouldRun*/

        return false

    }
}