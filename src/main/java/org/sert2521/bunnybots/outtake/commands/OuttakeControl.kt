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

        // If joystick button is pressed, open flap and start belt motor. If the button is not being pressed, close the flap and set the motor speed to 0 
        if (shouldOutput) {

            Outtake.beltMotor.set(Outtake.OUTTAKE_BELT_SPEED)
            Outtake.flapServo.angle = 90.0

        } else {

            Outtake.beltMotor.set(0.0)
            Outtake.flapServo.angle = 0.0

        }
        return false

    }
}