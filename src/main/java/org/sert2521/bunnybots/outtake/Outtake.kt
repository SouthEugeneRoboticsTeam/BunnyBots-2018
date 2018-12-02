package org.sert2521.bunnybots.outtake

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Servo
import org.sert2521.bunnybots.OUTTAKE_AUTO_SERVO
import org.sert2521.bunnybots.OUTTAKE_BELT_MOTOR
import org.sert2521.bunnybots.OUTTAKE_TELEOP_SERVO
import org.sertain.hardware.Talon
import org.sertain.hardware.setPercent
import org.team2471.frc.lib.framework.Subsystem

object Outtake : Subsystem("Outtake") {
    private val beltMotor = Talon(OUTTAKE_BELT_MOTOR)
    private val teleopServo = Servo(OUTTAKE_TELEOP_SERVO)
    private val autoServo = Servo(OUTTAKE_AUTO_SERVO)

    fun runBelt(speed: Double = OUTTAKE_BELT_SPEED) = beltMotor.setPercent(speed)

    fun open(auto: Boolean = DriverStation.getInstance().isAutonomous) {
        teleopServo.set(if (auto) TELEOP_CLOSED_POSITION else TELEOP_OPEN_POSITION)
        autoServo.set(if (auto) AUTO_OPEN_POSITION else AUTO_CLOSED_POSITION)
    }
}
