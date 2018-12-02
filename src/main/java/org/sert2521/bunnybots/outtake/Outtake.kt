package org.sert2521.bunnybots.outtake

import edu.wpi.first.wpilibj.Servo
import org.sert2521.bunnybots.outtake.commands.OuttakeControl
import org.sert2521.bunnybots.util.OUTTAKE_BELT_MOTOR
import org.sert2521.bunnybots.util.OUTTAKE_FLAP_SERVO
import org.sertain.command.Subsystem
import org.sertain.hardware.Talon

object Outtake : Subsystem() {
    const val OUTTAKE_BELT_SPEED = 0.0

    val beltMotor = Talon(OUTTAKE_BELT_MOTOR)
    val flapServo = Servo(OUTTAKE_FLAP_SERVO)

    override val defaultCommand get() = OuttakeControl()
}
