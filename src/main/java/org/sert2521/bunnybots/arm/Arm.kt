package org.sert2521.bunnybots.arm

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import org.sert2521.bunnybots.ARM_MOTOR
import org.sertain.hardware.Talon
import org.sertain.hardware.autoBrake
import org.sertain.hardware.getEncoderPosition
import org.sertain.hardware.setEncoderPosition
import org.sertain.hardware.setPIDF
import org.sertain.hardware.setPercent
import org.sertain.hardware.setPosition
import org.sertain.hardware.stop
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion_profiling.MotionCurve

object Arm : Subsystem("Arm") {
    private val armMotor = Talon(ARM_MOTOR).apply {
        setSensorPhase(false)
        autoBrake()
        configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0)
        setPIDF(kP = 0.25)
    }

    var animationTime: Double = 0.0
    var targetPose = ArmPose.TOP
    var motionCurve = MotionCurve()

    val isAnimationCompleted: Boolean
        get() = animationTime >= motionCurve.length

    var shouldRun = false
    var hasRun = false

    init {
        armMotor.setEncoderPosition(0)
    }

    val position get() = armMotor.getEncoderPosition()

    fun set(setpoint: Double) {
//        println("Going to point: $setpoint, at point: ${armMotor.getEncoderPosition()}, value: ${armMotor.motorOutputPercent}, error: ${armMotor.getClosedLoopError(0)}, target: ${armMotor.getClosedLoopTarget(0)}")
        armMotor.setPosition(setpoint)
    }

    fun setPose(pose: ArmPose) {
        set(pose.armPosition.toDouble())
    }

    fun setPercent(percent: Double) = armMotor.setPercent(percent)

    fun stop() = armMotor.stop()
}
