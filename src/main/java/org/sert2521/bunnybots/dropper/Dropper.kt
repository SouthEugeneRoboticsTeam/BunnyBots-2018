package org.sert2521.bunnybots.dropper

import edu.wpi.first.wpilibj.Servo
import kotlinx.coroutines.launch
import org.sert2521.bunnybots.LEFT_DROPPER_SERVO
import org.sert2521.bunnybots.MIDDLE_DROPPER_SERVO
import org.sert2521.bunnybots.RIGHT_DROPPER_SERVO
import org.sert2521.bunnybots.util.next
import org.team2471.frc.lib.coroutines.MeanlibScope
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.framework.Subsystem

object Dropper : Subsystem("Dropper") {
    private val leftServo = Servo(LEFT_DROPPER_SERVO)
    private val middleServo = Servo(MIDDLE_DROPPER_SERVO)
    private val rightServo = Servo(RIGHT_DROPPER_SERVO)

    private enum class DropperServo(val servo: Servo) {
        LEFT(leftServo),
        MIDDLE(middleServo),
        RIGHT(rightServo)
    }

    private var currentServo: DropperServo = DropperServo.LEFT

    fun reset() {
        leftServo.set(CLOSED_POSITION)
        middleServo.set(CLOSED_POSITION)
        rightServo.set(CLOSED_POSITION)

        currentServo = DropperServo.LEFT
    }

    fun dropNext() {
        val servo = currentServo.servo
        currentServo = currentServo.next()

        MeanlibScope.launch {
            servo.set(OPEN_POSITION)
            delay(DELAY_TIME)
            servo.set(CLOSED_POSITION)
            delay(DELAY_TIME)
            servo.setDisabled()
        }
    }
}
