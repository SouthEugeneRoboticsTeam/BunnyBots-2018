package org.sert2521.bunnybots.intake

import org.sert2521.bunnybots.INTAKE_MOTOR
import org.sertain.hardware.Talon
import org.sertain.hardware.setPercent
import org.sertain.hardware.stop
import org.team2471.frc.lib.framework.Subsystem

object Intake : Subsystem("Intake") {
    private val intakeMotor = Talon(INTAKE_MOTOR)

    fun run(speed: Double = INTAKE_SPEED) = intakeMotor.setPercent(speed)

    fun stop() = intakeMotor.stop()
}
