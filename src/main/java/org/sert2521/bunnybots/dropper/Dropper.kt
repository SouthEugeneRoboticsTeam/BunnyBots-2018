package org.sert2521.bunnybots.dropper

import edu.wpi.first.wpilibj.Servo
import org.sert2521.bunnybots.dropper.commands.DropperControl
import org.sert2521.bunnybots.util.LEFT_DROPPER_SERVO
import org.sert2521.bunnybots.util.MIDDLE_DROPPER_SERVO
import org.sert2521.bunnybots.util.RIGHT_DROPPER_SERVO
import org.sertain.command.Subsystem

object Dropper : Subsystem() {

    val leftServo = Servo(LEFT_DROPPER_SERVO)
    val middleServo = Servo(MIDDLE_DROPPER_SERVO)
    val rightServo = Servo(RIGHT_DROPPER_SERVO)

    override val defaultCommand get() = DropperControl()

}