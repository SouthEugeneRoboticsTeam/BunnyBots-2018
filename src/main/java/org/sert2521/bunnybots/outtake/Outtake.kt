package org.sert2521.bunnybots.outtake

import edu.wpi.first.wpilibj.Servo
import org.sert2521.bunnybots.OUTTAKE_AUTO_SERVO
import org.sert2521.bunnybots.OUTTAKE_BELT_MOTOR
import org.sert2521.bunnybots.OUTTAKE_TELEOP_SERVO
import org.sertain.hardware.Talon
import org.sertain.hardware.setPercent
import org.team2471.frc.lib.framework.Subsystem
import kotlin.math.abs

object Outtake : Subsystem("Outtake") {
    private val beltMotor = Talon(OUTTAKE_BELT_MOTOR)
    private val teleopServo = Servo(OUTTAKE_TELEOP_SERVO)
    private val autoServo = Servo(OUTTAKE_AUTO_SERVO)

    var teleopOpen = false

    fun run(speed: Double = OUTTAKE_BELT_SPEED) = beltMotor.setPercent(abs(speed) * if (teleopOpen) -1 else 1)

    fun stop() = beltMotor.stopMotor()

    fun toggle() = if (teleopOpen) openAuto() else openTeleop()

    fun openAuto() {
        teleopOpen = false

        teleopServo.set(TELEOP_CLOSED_POSITION)
        autoServo.set(AUTO_OPEN_POSITION)
    }

    fun closeAuto() {
        autoServo.set(AUTO_CLOSED_POSITION)
    }

    fun openTeleop() {
        teleopOpen = true

        teleopServo.set(TELEOP_OPEN_POSITION)
        autoServo.set(AUTO_CLOSED_POSITION)
    }

    fun closeTeleop() {
        teleopServo.set(AUTO_CLOSED_POSITION)
    }
}
