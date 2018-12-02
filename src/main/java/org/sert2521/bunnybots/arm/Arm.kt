package org.sert2521.bunnybots.arm

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import org.sert2521.bunnybots.ARM_MOTOR
import org.sertain.hardware.Talon
import org.sertain.hardware.setPIDF
import org.sertain.hardware.setPosition
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.motion_profiling.MotionCurve

object Arm : Subsystem("Arm") {
    val armMotor = Talon(ARM_MOTOR).apply {
        setSensorPhase(true)
//        inverted = true
        configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0)
        setPIDF(kP = 1.0, kD = 20.0)
    }

    var animationTime: Double = 0.0
    var targetPose = Pose.TOP
    var motionCurve = MotionCurve()

    val isAnimationCompleted: Boolean
        get() = animationTime >= motionCurve.length

    var shouldRun = false
    var hasRun = false

    fun set(setpoint: Double, velocity: Double = 0.0) {
//        println("Going to point: $setpoint, at point: ${armMotor.getEncoderPosition()}, value: ${armMotor.motorOutputPercent}, error: ${armMotor.getClosedLoopError(0)}, target: ${armMotor.getClosedLoopTarget(0)}")
        armMotor.setPosition(setpoint, velocity * ARM_VELOCITY_FEED_FORWARD)
    }

    fun setPose(pose: Pose) {
        set(pose.armPosition.toDouble())
    }
}
