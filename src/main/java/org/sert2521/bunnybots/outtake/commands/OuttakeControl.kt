package org.sert2521.bunnybots.outtake.commands

import org.sert2521.bunnybots.outtake.Outtake
import org.sert2521.bunnybots.util.OUTTAKE_BELT_MOTOR
import org.sert2521.bunnybots.util.secondaryJoystick
import org.sertain.command.Command
import edu.wpi.first.wpilibj.Servo

class OuttakeControl : Command() {
    init {
        requires(Outtake)
    }

    private val shouldOutput get() = secondaryJoystick.getRawButton(-1)

    override fun execute(): Boolean {
        /*Outtake.motor.set(if (shouldRun) Outtake.OUTTAKE_SPEED else 0.0)
        if (shouldToggle) shouldRun = !shouldRun*/
        if (shouldOutput) {

            Outtake.beltMotor.set(Outtake.OUTTAKE_BELT_SPEED)
            Outtake.flapServo.setAngle(0.0)
            
        }

        return false

    }
}