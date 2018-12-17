package org.sert2521.bunnybots.drivetrain

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import kotlinx.coroutines.launch
import org.sert2521.bunnybots.ENCODER_TICKS_PER_REVOLUTION
import org.sert2521.bunnybots.LEFT_FRONT_MOTOR
import org.sert2521.bunnybots.LEFT_REAR_MOTOR
import org.sert2521.bunnybots.RIGHT_FRONT_MOTOR
import org.sert2521.bunnybots.RIGHT_REAR_MOTOR
import org.sert2521.bunnybots.WHEEL_DIAMETER
import org.sert2521.bunnybots.util.Telemetry
import org.sertain.hardware.Talon
import org.sertain.hardware.getEncoderPosition
import org.sertain.hardware.invert
import org.sertain.hardware.plus
import org.sertain.hardware.setBrake
import org.sertain.hardware.setEncoderPosition
import org.sertain.hardware.setPIDF
import org.sertain.hardware.setPercent
import org.sertain.hardware.setPosition
import org.sertain.hardware.setSelectedSensor
import org.team2471.frc.lib.coroutines.MeanlibScope
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.vector.Vector2
import java.lang.Math.signum
import java.lang.Math.toDegrees
import kotlin.math.abs
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

    private val lidar = AnalogInput(0)

    private val lidarList = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    // Get LiDAR distance in inches
    private val lidarDistance get() = ((52.993 / Math.pow(lidar.averageVoltage, 0.158)) - 41.789) / 2.54

    private val averageLidarDistance: Double? get() {
        return try {
            ((52.993 / Math.pow(lidarList.toList().average(), 0.158)) - 41.789) / 2.54
        } catch (exception: Exception) {
            null
        }
    }

    private val leftPosition get() = leftDrive.getEncoderPosition()
    private val rightPosition get() = rightDrive.getEncoderPosition()

    private val leftDistance get() = ticksToFeet(leftPosition)
    private val rightDistance get() = ticksToFeet(rightPosition)

    private val leftDrive = Talon(LEFT_FRONT_MOTOR).setBrake() + Talon(LEFT_REAR_MOTOR).setBrake()
    private val rightDrive =
            Talon(RIGHT_FRONT_MOTOR).setBrake().invert() + Talon(RIGHT_REAR_MOTOR).setBrake().invert()
    private val drive = DifferentialDrive(leftDrive, rightDrive)

    init {
        leftDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)
        rightDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)

        leftDrive.setSensorPhase(true)
        rightDrive.setSensorPhase(true)

        leftDrive.configAllowableClosedloopError(0, 0, 0)
        rightDrive.configAllowableClosedloopError(0, 0, 0)

        val kF = 0.0

        // (max % output / 1023) / encoder ticks per rev
        val kP = (0.75 * 1023.0) / 4096.0

        leftDrive.setPIDF(kP = kP, kF = kF)
        rightDrive.setPIDF(kP = kP, kF = kF)

        telemetry.add("Left Encoder") { leftPosition }
        telemetry.add("Right Encoder") { rightPosition }
        telemetry.add("Gyro") { ahrs.angle }
        telemetry.add("LiDAR Distance (in)") { averageLidarDistance ?: 0.0 }

        drive.isSafetyEnabled = false

        reset()

        MeanlibScope.launch {
            periodic(0.05) {
                val lidarValue: Double? = lidar.averageVoltage
                lidarList.removeAt(0)
                lidarList.add(lidarValue?.coerceAtLeast(0.1) ?: 0.1)
            }
        }
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

    suspend fun driveAlongPath(path: Path2D, extraTime: Double = 0.0, useLidar: Boolean = false, forward: Boolean = true) {
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

        var prevLidarDistance: Double? = null
        var accumulator = 0.0
        var finished: Boolean

        var lastSetTime = timer.get()

        try {
            periodic(watchOverrun = false) {
                val t = timer.get()
                val dt = t - prevTime

                val gyroAngle = ahrs.angle
                val pathAngle = toDegrees(Vector2.angle(path.getTangent(t)))
                val angleError = pathAngle - windRelativeAngles(pathAngle, gyroAngle)

                val correction = if (useLidar) {
                    // Ensure we've travelled far enough for LiDAR to work
                    if (leftDistance > 2.25 || (leftDistance > 0.75 && !forward)) {
                        // LiDAR will jump when we've reached the end of the crates, so stop then
                        val jump = abs(lidarDistance) - abs(prevLidarDistance ?: lidarDistance)
                        if (jump > 5.0) exitPeriodic()

                        val lidarError = (if (averageLidarDistance != null) {
                            prevLidarDistance = averageLidarDistance ?: 0.0
                            averageLidarDistance ?: 0.0
                        } else {
                            prevLidarDistance ?: 0.0
                        } - LIDAR_SETPOINT) * -1

                        accumulator = accumulator * LIDAR_CORRECTION_I_DECAY + lidarError
                        lidarError * LIDAR_CORRECTION_P + accumulator * LIDAR_CORRECTION_I
                    } else {
                        0.0
                    }
                } else {
                    accumulator = accumulator * GYRO_CORRECTION_I_DECAY + angleError
                    angleError * GYRO_CORRECTION_P + accumulator * GYRO_CORRECTION_I
                } * if (forward) 1 else -1

                val leftDistance = path.getLeftDistance(t) + correction
                val rightDistance = path.getRightDistance(t) - correction

                val leftVelocity = (leftDistance - prevLeftDistance) / dt
                val rightVelocity = (rightDistance - prevRightDistance) / dt

                val leftVelocityError = leftDrive.getSelectedSensorVelocity(0) - leftVelocity
                val rightVelocityError = rightDrive.getSelectedSensorVelocity(0) - rightVelocity

                val velocityDelta = (leftVelocity - rightVelocity) * TURNING_FEED_FORWARD

                pathAngleEntry.setDouble(pathAngle)
                angleErrorEntry.setDouble(angleError)
                leftPositionErrorEntry.setDouble(ticksToFeet(leftDrive.getClosedLoopError(0)))
                rightPositionErrorEntry.setDouble(ticksToFeet(rightDrive.getClosedLoopError(0)))
                leftVelocityErrorEntry.setDouble(ticksToFeet(round(leftVelocityError * 10).toInt()))
                rightVelocityErrorEntry.setDouble(ticksToFeet(round(rightVelocityError * 10).toInt()))
                leftPositionEntry.setDouble(leftDistance)
                rightPositionEntry.setDouble(rightDistance)
                gyroCorrectionEntry.setDouble(correction)
                leftPercentage.setDouble(leftDrive.motorOutputPercent)
                rightPercentage.setDouble(rightDrive.motorOutputPercent)

                val leftFeedForward = leftVelocity * LEFT_FEED_FORWARD_COEFFICIENT +
                        (LEFT_FEED_FORWARD_OFFSET * signum(leftVelocity)) + velocityDelta

                val rightFeedForward = rightVelocity * RIGHT_FEED_FORWARD_COEFFICIENT +
                        (RIGHT_FEED_FORWARD_OFFSET * signum(rightVelocity)) - velocityDelta

                drivePosition(feetToTicks(leftDistance), feetToTicks(rightDistance), leftFeedForward, rightFeedForward)

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

    fun drivePosition(
        leftPosition: Double,
        rightPosition: Double,
        leftFeedForward: Double? = null,
        rightFeedForward: Double? = null
    ) {
        leftDrive.setPosition(leftPosition, leftFeedForward)
        rightDrive.setPosition(rightPosition, rightFeedForward)
    }

    fun stop() {
        leftDrive.setPercent(0.0)
        rightDrive.setPercent(0.0)
    }
}
