package org.sert2521.bunnybots.drivetrain

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.sert2521.bunnybots.ENCODER_TICKS_PER_REVOLUTION
import org.sert2521.bunnybots.LEFT_FRONT_MOTOR
import org.sert2521.bunnybots.LEFT_REAR_MOTOR
import org.sert2521.bunnybots.RIGHT_FRONT_MOTOR
import org.sert2521.bunnybots.RIGHT_REAR_MOTOR
import org.sert2521.bunnybots.WHEEL_DIAMETER
import org.sert2521.bunnybots.util.Telemetry
import org.sertain.hardware.Talon
import org.sertain.hardware.autoBreak
import org.sertain.hardware.getEncoderPosition
import org.sertain.hardware.invert
import org.sertain.hardware.plus
import org.sertain.hardware.setEncoderPosition
import org.sertain.hardware.setSelectedSensor
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.vector.Vector2
import java.lang.Math.toDegrees
import kotlin.math.round

/**
 * The robot's drive system.
 */
object Drivetrain : Subsystem("Drivetrain") {
    private val telemetry = Telemetry(this)

    val ahrs = AHRS(I2C.Port.kMXP)

    private fun ticksToFeet(ticks: Int) =
            ticks.toDouble() / ENCODER_TICKS_PER_REVOLUTION * WHEEL_DIAMETER * Math.PI / 12.0

    private fun feetToTicks(feet: Double) =
            feet * 12.0 / Math.PI / WHEEL_DIAMETER * ENCODER_TICKS_PER_REVOLUTION

    private val leftPosition get() = leftDrive.getEncoderPosition()
    private val rightPosition get() = rightDrive.getEncoderPosition()

    private val leftDistance get() = ticksToFeet(leftPosition)
    private val rightDistance get() = ticksToFeet(rightPosition)

    private val leftDrive = Talon(LEFT_FRONT_MOTOR).autoBreak() + Talon(LEFT_REAR_MOTOR).autoBreak()
    private val rightDrive =
            Talon(RIGHT_FRONT_MOTOR).autoBreak().invert() + Talon(RIGHT_REAR_MOTOR).autoBreak().invert()
    private val drive = DifferentialDrive(leftDrive, rightDrive)

    init {
        leftDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)
        rightDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)

        leftDrive.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 20, 0)
        rightDrive.setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, 20, 0)
        leftDrive.setStatusFramePeriod(StatusFrameEnhanced.Status_3_Quadrature, 20, 0)
        rightDrive.setStatusFramePeriod(StatusFrameEnhanced.Status_3_Quadrature, 20, 0)

        leftDrive.setSensorPhase(true)
        rightDrive.setSensorPhase(true)

        leftDrive.configAllowableClosedloopError(0, 0, 0)
        rightDrive.configAllowableClosedloopError(0, 0, 0)

        // (% output / 1023.0) / speed @ % output
        val kF = (0.48 * 1023.0) / 9000.0

        // (max % output @ 1 rev / 1023) / encoder ticks per rev
        val kP = (0.4 * 1023.0) / 8192.0

        leftDrive.config_kF(0, kF, 0)
        leftDrive.config_kP(0, kP, 0)
        leftDrive.config_kI(0, 0.0, 0)
        leftDrive.config_kD(0, 0.0, 0)

        rightDrive.config_kF(0, kF, 0)
        rightDrive.config_kP(0, kP, 0)
        rightDrive.config_kI(0, 0.0, 0)
        rightDrive.config_kD(0, 0.0, 0)

        telemetry.add("Left Encoder") { leftPosition }
        telemetry.add("Right Encoder") { rightPosition }
        telemetry.add("Gyro") { ahrs.angle }

        drive.isSafetyEnabled = false

        reset()
    }

    fun reset() {
        leftDrive.setEncoderPosition(0)
        rightDrive.setEncoderPosition(0)
        ahrs.reset()
    }

    fun driveRaw(left: Double, right: Double) = drive.tankDrive(left, -right, false)

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

        val leftPercentage = telemetry.table.getEntry("Left Percentage")
        val rightPercentage = telemetry.table.getEntry("Right Percentage")

        val lastSet = telemetry.table.getEntry("Time Between")

        val timer = Timer().apply { start() }

        var angleErrorAccumulator = 0.0
        var finished: Boolean

        var lastSetTime = timer.get()

        try {
            periodic(watchOverrun = false) {
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

                pathAngleEntry.setDouble(pathAngle)
                angleErrorEntry.setDouble(pathAngle)
                leftPositionErrorEntry.setDouble(ticksToFeet(leftDrive.getClosedLoopError(0)))
                rightPositionErrorEntry.setDouble(ticksToFeet(rightDrive.getClosedLoopError(0)))
                leftVelocityErrorEntry.setDouble(ticksToFeet(round(leftVelocityError * 10).toInt()))
                rightVelocityErrorEntry.setDouble(ticksToFeet(round(rightVelocityError * 10).toInt()))
                leftPositionEntry.setDouble(leftDistance)
                rightPositionEntry.setDouble(rightDistance)
                gyroCorrectionEntry.setDouble(gyroCorrection)
                leftPercentage.setDouble(leftDrive.motorOutputPercent)
                rightPercentage.setDouble(rightDrive.motorOutputPercent)

                leftDrive.set(ControlMode.Position, feetToTicks(leftDistance))
                rightDrive.set(ControlMode.Position, feetToTicks(rightDistance))

                lastSet.setDouble(lastSetTime - timer.get())
                lastSetTime = timer.get()

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

                if (finished) {
                    exitPeriodic()
                }
            }
        } finally {
            println("I am done, stopping motors!")
            stop()
        }
    }

    fun drivePosition(leftPosition: Double, rightPosition: Double) {
        leftDrive.set(ControlMode.Position, leftPosition)
        rightDrive.set(ControlMode.Position, rightPosition)
    }

    fun stop() {
        leftDrive.set(0.0)
        rightDrive.set(0.0)
    }
}
