package org.sert2521.bunnybots.drivetrain

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlinx.coroutines.launch
import org.sert2521.bunnybots.util.ENCODER_TICKS_PER_METER
import org.sert2521.bunnybots.util.ENCODER_TICKS_PER_REVOLUTION
import org.sert2521.bunnybots.util.LEFT_FRONT_MOTOR
import org.sert2521.bunnybots.util.LEFT_REAR_MOTOR
import org.sert2521.bunnybots.util.PositionEstimator
import org.sert2521.bunnybots.util.RIGHT_FRONT_MOTOR
import org.sert2521.bunnybots.util.RIGHT_REAR_MOTOR
import org.sert2521.bunnybots.util.Telemetry
import org.sert2521.bunnybots.util.WHEEL_DIAMETER
import org.sertain.hardware.Talon
import org.sertain.hardware.autoBreak
import org.sertain.hardware.getEncoderPosition
import org.sertain.hardware.getEncoderVelocity
import org.sertain.hardware.invert
import org.sertain.hardware.plus
import org.sertain.hardware.setEncoderPosition
import org.sertain.hardware.setSelectedSensor
import org.team2471.frc.lib.coroutines.MeanlibScope
import org.team2471.frc.lib.coroutines.loop
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.math.Point
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.vector.Vector2
import java.lang.Math.signum
import java.lang.Math.toDegrees

/**
 * The robot's drive system.
 */
object Drivetrain : Subsystem("Drivetrain") {
    private val telemetry = Telemetry(this)

    val ahrs = AHRS(I2C.Port.kMXP)
    val isNavxBroken get() = angles.all { it == angles.first() }
    private val angles = mutableListOf<Double>()

    private fun ticksToFeet(ticks: Int) =
            ticks.toDouble() / ENCODER_TICKS_PER_REVOLUTION * WHEEL_DIAMETER * Math.PI / 12.0

    private fun feetToTicks(feet: Double) =
            feet * 12.0 / Math.PI / WHEEL_DIAMETER * ENCODER_TICKS_PER_REVOLUTION

    private val leftPosition get() = leftDrive.getEncoderPosition()
    private val rightPosition get() = rightDrive.getEncoderPosition()

    private val leftDistance get() = ticksToFeet(leftPosition)
    private val rightDistance get() = ticksToFeet(rightPosition)

    internal val leftDrive =
            Talon(LEFT_FRONT_MOTOR).autoBreak() + Talon(LEFT_REAR_MOTOR).autoBreak()
    internal val rightDrive =
            Talon(RIGHT_FRONT_MOTOR).autoBreak().invert(true) + Talon(RIGHT_REAR_MOTOR).autoBreak().invert(true)
    private val drive = DifferentialDrive(leftDrive, rightDrive)

    private val positionEstimator = PositionEstimator(Point(0.0, 0.0), 0.0)
    val estimatedPosition get() = positionEstimator.position

    init {
        leftDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)
        rightDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)

        leftDrive.setSensorPhase(true)
        rightDrive.setSensorPhase(true)

        leftDrive.configAllowableClosedloopError(0, 100, 0)
        rightDrive.configAllowableClosedloopError(0, 100, 0)

        leftDrive.config_kF(0, 0.0, 0)
        leftDrive.config_kP(0, 0.45, 0)
        leftDrive.config_kI(0, 0.0, 0)
        leftDrive.config_kD(0, 0.35, 0)

        rightDrive.config_kF(0, 0.0, 0)
        rightDrive.config_kP(0, 0.45, 0)
        rightDrive.config_kI(0, 0.0, 0)
        rightDrive.config_kD(0, 0.35, 0)

        telemetry.add("Left Encoder") { leftPosition }
        telemetry.add("Right Encoder") { rightPosition }
        telemetry.add("NavX Broken?") { isNavxBroken }

        reset()
        MeanlibScope.launch {
            loop {
                updateStoredAngles()
                updateLocalization()
            }
        }
    }

    private fun updateStoredAngles() {
        angles.removeAt(0)
        angles.add(ahrs.angle)
    }

    private fun updateLocalization() {
        positionEstimator.updatePosition(
                ticksToFeet(leftDrive.getEncoderVelocity()),
                ticksToFeet(rightDrive.getEncoderVelocity()),
                ahrs.angle
        )

        println(estimatedPosition)
    }

    fun reset() {
        leftDrive.setEncoderPosition(0)
        rightDrive.setEncoderPosition(0)
        ahrs.reset()
    }

    fun driveRaw(left: Double, right: Double) = drive.tankDrive(left, right, false)

    fun tank(left: Double, right: Double) = drive.tankDrive(left, -right)

    fun arcade(speed: Double, rotation: Double) = drive.arcadeDrive(speed, rotation)

    fun curvature(speed: Double, rotation: Double, quickTurn: Boolean) =
            drive.curvatureDrive(speed, rotation, quickTurn)

    suspend fun driveDistance(distance: Double, time: Double, suspend: Boolean = true) {
        leftDrive.selectProfileSlot(0, 0)
        rightDrive.selectProfileSlot(0, 0)

        val curve = MotionCurve().apply {
            storeValue(0.0, 0.0)
            storeValue(time, distance)
        }

        try {
            reset()

            val timer = Timer().apply { start() }
            periodic {
                val t = timer.get()
                val v = curve.getValue(t)

                println("$t, $v, $leftDistance, ${feetToTicks(v)}, $leftPosition")

                drivePosition(feetToTicks(v), feetToTicks(v))

                timer.get() >= time
            }

            if (suspend) {
                suspendUntil {
                    Math.abs(leftDistance - distance) < 0.1 && Math.abs(rightDistance - distance) < 0.1
                }
            }
        } finally {
            drive.stopMotor()
        }
    }

    suspend fun driveAlongPath(path: Path2D, extraTime: Double = 0.0) {
        println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}," +
                        "travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}")

        path.resetDistances()
        reset()

        var prevLeftDistance = 0.0
        var prevRightDistance = 0.0
        var prevLeftVelocity = 0.0
        var prevRightVelocity = 0.0
        var prevTime = 0.0

        val pathAngleEntry = telemetry.table.getEntry("Path Angle")
        val angleErrorEntry = telemetry.table.getEntry("Angle Error")
        val gyroCorrectionEntry = telemetry.table.getEntry("Gyro Correction")

        val leftPositionErrorEntry = telemetry.table.getEntry("Left Position Error")
        val rightPositionErrorEntry = telemetry.table.getEntry("Right Position Error")

        val leftVelocityErrorEntry = telemetry.table.getEntry("Left Velocity Error")
        val rightVelocityErrorEntry = telemetry.table.getEntry("Right Velocity Error")

        val leftPositionEntry = telemetry.table.getEntry("Left Position")
        val rightPositionEntry = telemetry.table.getEntry("Right Position")

        val timer = Timer().apply { start() }

        var angleErrorAccumulator = 0.0
        var finished: Boolean
        try {
            periodic {
                val t = timer.get()
                val dt = t - prevTime

                val gyroAngle = ahrs.angle
                val pathAngle = toDegrees(Vector2.angle(path.getTangent(t)))
                val angleError = pathAngle - windRelativeAngles(pathAngle, gyroAngle)

                angleErrorAccumulator = angleErrorAccumulator * GYRO_CORRECTION_I_DECAY + angleError

                val gyroCorrection = if (SmartDashboard.getBoolean("Use Gyro", true)) {
                    angleError * GYRO_CORRECTION_P + angleErrorAccumulator * GYRO_CORRECTION_I
                } else {
                    0.0
                }

                val leftDistance = path.getLeftDistance(t) + gyroCorrection
                val rightDistance = path.getRightDistance(t) - gyroCorrection

                val leftVelocity = (leftDistance - prevLeftDistance) / dt
                val rightVelocity = (rightDistance - prevRightDistance) / dt

                val leftVelocityError = leftDrive.getSelectedSensorVelocity(0) - leftVelocity
                val rightVelocityError = rightDrive.getSelectedSensorVelocity(0) - rightVelocity

                val velocityDelta = (leftVelocity - rightVelocity) * TURNING_FEED_FORWARD

                pathAngleEntry.setDouble(pathAngle)
                angleErrorEntry.setDouble(pathAngle)
                leftPositionErrorEntry.setDouble(leftDrive.getClosedLoopError(0) / ENCODER_TICKS_PER_METER)
                rightPositionErrorEntry.setDouble(rightDrive.getClosedLoopError(0) / ENCODER_TICKS_PER_METER)
                leftVelocityErrorEntry.setDouble(leftVelocityError)
                rightVelocityErrorEntry.setDouble(rightVelocityError)
                leftPositionEntry.setDouble(leftDistance)
                rightPositionEntry.setDouble(rightDistance)
                gyroCorrectionEntry.setDouble(gyroCorrection)

                val leftFeedForward = leftVelocity * LEFT_FEED_FORWARD_COEFFICIENT +
                        (LEFT_FEED_FORWARD_OFFSET * signum(leftVelocity)) + velocityDelta

                val rightFeedForward = rightVelocity * RIGHT_FEED_FORWARD_COEFFICIENT +
                        (RIGHT_FEED_FORWARD_OFFSET * signum(rightVelocity)) - velocityDelta

                println("Gyro Correction: $gyroCorrection, Left FF: $leftFeedForward, Right FF: $rightFeedForward")

                leftDrive.set(ControlMode.Position, feetToTicks(leftDistance),
                    DemandType.ArbitraryFeedForward, leftFeedForward)
                rightDrive.set(ControlMode.Position, feetToTicks(rightDistance),
                    DemandType.ArbitraryFeedForward, rightFeedForward)

                if (leftDrive.motorOutputPercent > 0.95) {
                    DriverStation.reportWarning("Left motor is saturated", false)
                }
                if (rightDrive.motorOutputPercent > 0.95) {
                    DriverStation.reportWarning("Right motor is saturated", false)
                }

                finished = t >= path.durationWithSpeed + extraTime

                prevTime = t
                prevLeftDistance = leftDistance
                prevRightDistance = rightDistance
                prevLeftVelocity = leftVelocity
                prevRightVelocity = rightVelocity

                finished
            }
        } finally {
            drive.stopMotor()
        }
    }

    fun drivePosition(leftPosition: Double, rightPosition: Double) {
        leftDrive.set(ControlMode.Position, leftPosition)
        rightDrive.set(ControlMode.Position, rightPosition)
    }

    fun stop() = drive.stopMotor()

    init {
        drive.isSafetyEnabled = false
    }
}
