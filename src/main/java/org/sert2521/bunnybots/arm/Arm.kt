package org.sert2521.bunnybots.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import org.sert2521.bunnybots.util.ARM_MOTOR
import org.sertain.command.Subsystem
import org.sertain.hardware.Talon
import org.team2471.frc.lib.motion_profiling.MotionCurve

private const val ARM_VELOCITY_FEED_FORWARD = 0.00005

object Arm : Subsystem() {
    val armMotor = Talon(ARM_MOTOR).apply {
        setSensorPhase(true)
//        inverted = true
        configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0)
        config_kP(0, 1.0, 10)
        config_kI(0, 0.0, 10)
        config_kD(0, 20.0, 10)
        config_kF(0, 0.0, 10)
//        configClosedloopRamp(1.0, 0)
        configClosedloopRamp(0.0, 10)
        selectProfileSlot(0, 0)
        configClosedLoopPeriod(0, 1, 10)
    }

    var animationTime: Double = 0.0
    var targetPose = Pose.TOP
    var motionCurve = MotionCurve()

    val isAnimationCompleted: Boolean
        get() = animationTime >= motionCurve.length

    var shouldRun = false
    var hasRun = false
    override fun onTeleopStart() {
        shouldRun = false
        hasRun = false

        armMotor.setSelectedSensorPosition(0, 0, 10)
//        armMotor.setEncoderPosition(0)
    }

    override fun executeTeleop() {
//        if (shouldRun && !hasRun) {
//            setPose(Pose.TOP)
//            hasRun = true
//        }
//
//        shouldRun = true
    }

    fun set(setpoint: Double, velocity: Double = 0.0) {
//        println("Going to point: $setpoint, at point: ${armMotor.getEncoderPosition()}, value: ${armMotor.motorOutputPercent}, error: ${armMotor.getClosedLoopError(0)}, target: ${armMotor.getClosedLoopTarget(0)}")
        armMotor.set(ControlMode.Position,
            setpoint,
            DemandType.ArbitraryFeedForward,
            velocity * ARM_VELOCITY_FEED_FORWARD)
    }

    fun setPose(pose: Pose) {
        set(pose.armPosition.toDouble())
    }
}
