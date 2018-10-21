package org.sert2521.bunnybots.drivetrain

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.I2C
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.drive.DifferentialDrive
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.sert2521.bunnybots.drivetrain.commands.TeleopDrive
import org.sert2521.bunnybots.util.ENCODER_TICKS_PER_METER
import org.sert2521.bunnybots.util.LEFT_FRONT_MOTOR
import org.sert2521.bunnybots.util.LEFT_REAR_MOTOR
import org.sert2521.bunnybots.util.RIGHT_FRONT_MOTOR
import org.sert2521.bunnybots.util.RIGHT_REAR_MOTOR
import org.sertain.command.Subsystem
import org.sertain.hardware.Talon
import org.sertain.hardware.autoBreak
import org.sertain.hardware.getEncoderPosition
import org.sertain.hardware.plus
import org.sertain.hardware.setEncoderPosition
import org.sertain.hardware.setSelectedSensor
import org.team2471.frc.lib.control.experimental.periodic
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.vector.Vector2
import java.lang.Math.signum
import java.lang.Math.toDegrees

private const val GYRO_CORRECTION_P = 0.025 * 0.75
private const val GYRO_CORRECTION_I = 0.002 * 0.5
private const val GYRO_CORRECTION_I_DECAY = 1.0

private const val LEFT_FEED_FORWARD_COEFFICIENT = 0.070541988198899
private const val LEFT_FEED_FORWARD_OFFSET = 0.021428882425651

private const val RIGHT_FEED_FORWARD_COEFFICIENT = 0.071704891069425
private const val RIGHT_FEED_FORWARD_OFFSET = 0.020459379452296

private const val TURNING_FEED_FORWARD = 0.0324

/**
 * The robot's primary drive base.
 */
object Drivetrain : Subsystem() {
    val ahrs = AHRS(I2C.Port.kMXP)
    val isNavxBroken get() = angles.all { it == angles.first() }
    private val angles = mutableListOf<Double>()

    private val table = NetworkTableInstance.getDefault().getTable("Drivetrain")

    val leftPosition get() = leftDrive.getEncoderPosition()
    val rightPosition get() = rightDrive.getEncoderPosition()

    private val leftDrive =
            Talon(LEFT_FRONT_MOTOR).autoBreak() + Talon(LEFT_REAR_MOTOR).autoBreak()
    private val rightDrive =
            Talon(RIGHT_FRONT_MOTOR).autoBreak() + Talon(RIGHT_REAR_MOTOR).autoBreak()
    private val drive = DifferentialDrive(leftDrive, rightDrive)

    override val defaultCommand = TeleopDrive()

    override fun onCreate() {
        leftDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)
        rightDrive.setSelectedSensor(FeedbackDevice.QuadEncoder)

        leftDrive.setSensorPhase(true)

        leftDrive.config_kP(0, SmartDashboard.getNumber("Drive kP", 0.0), 0)
        leftDrive.config_kI(0, SmartDashboard.getNumber("Drive kI", 0.0), 0)
        leftDrive.config_kD(0, SmartDashboard.getNumber("Drive kD", 0.0), 0)

        rightDrive.config_kP(0, SmartDashboard.getNumber("Drive kP", 0.0), 0)
        rightDrive.config_kI(0, SmartDashboard.getNumber("Drive kI", 0.0), 0)
        rightDrive.config_kD(0, SmartDashboard.getNumber("Drive kD", 0.0), 0)
    }

    override fun onStart() {
        reset()

        angles.clear()
        angles.addAll(generateSequence(0.0) { it + 1 }.take(50))
    }

    override fun execute() {
        SmartDashboard.putNumber("Drivetrain Left Position", leftPosition.toDouble())
        SmartDashboard.putNumber("Drivetrain Right Position", rightPosition.toDouble())
        SmartDashboard.putNumber("Drivetrain Pitch", ahrs.pitch.toDouble())
        SmartDashboard.putNumber("Drivetrain Roll", ahrs.roll.toDouble())
        SmartDashboard.putData("AHRS", ahrs)
    }

    override fun executeAuto() {
        updateStoredAngles()
    }

    override fun executeTeleop() {
        updateStoredAngles()
    }

    private fun updateStoredAngles() {
        angles.removeAt(0)
        angles.add(ahrs.angle)
    }

    fun reset() {
        leftDrive.setEncoderPosition(0)
        rightDrive.setEncoderPosition(0)
        ahrs.reset()
    }

    fun arcade(speed: Double, rotation: Double) {
        logArcade(speed, rotation)
        drive.arcadeDrive(speed, rotation)
    }

    fun curvature(speed: Double, rotation: Double, quickTurn: Boolean) {
        logArcade(speed, rotation, quickTurn)
        drive.curvatureDrive(speed, rotation, quickTurn)
    }

    fun tank(left: Double, right: Double) {
        logTank(left, right)
        drive.tankDrive(left, -right)
    }

    fun drive(left: Double, right: Double) {
        logTank(left, right)
        leftDrive.set(left)
        rightDrive.set(-right)
    }

    suspend fun driveDistance(distance: Double, time: Double, suspend: Boolean = true) {
        val curve = MotionCurve().apply {
            storeValue(0.0, 0.0)
            storeValue(time, distance)
        }

        try {
            reset()

            val timer = Timer().apply { start() }
            periodic(condition = { timer.get() <= time }) {
                val t = timer.get()
                val v = curve.getValue(t)

                drivePosition(v, v)
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

        val pathAngleEntry = table.getEntry("Path Angle")
        val angleErrorEntry = table.getEntry("Angle Error")
        val gyroCorrectionEntry = table.getEntry("Gryo Correction")

        val leftPositionErrorEntry = table.getEntry("Left Position Error")
        val rightPositionErrorEntry = table.getEntry("Right Position Error")

        val leftVelocityErrorEntry = table.getEntry("Left Velocity Error")
        val rightVelocityErrorEntry = table.getEntry("Right Velocity Error")

        val timer = Timer().apply { start() }

        var angleErrorAccumulator = 0.0
        var finished = false
        try {
            periodic(condition = { !finished }) {
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

                val leftVelocityError = leftDrive.getSelectedSensorVelocity(0) + leftVelocity
                val rightVelocityError = rightDrive.getSelectedSensorVelocity(0) - rightVelocity

                val velocityDelta = (leftVelocity - rightVelocity) * TURNING_FEED_FORWARD

                pathAngleEntry.setDouble(pathAngle)
                angleErrorEntry.setDouble(pathAngle)
                leftPositionErrorEntry.setDouble(leftDrive.getClosedLoopError(0) / ENCODER_TICKS_PER_METER)
                rightPositionErrorEntry.setDouble(rightDrive.getClosedLoopError(0) / ENCODER_TICKS_PER_METER)
                leftVelocityErrorEntry.setDouble(leftVelocityError)
                rightVelocityErrorEntry.setDouble(rightVelocityError)
                gyroCorrectionEntry.setDouble(gyroCorrection)

                val leftFeedForward = leftVelocity * LEFT_FEED_FORWARD_COEFFICIENT +
                        (LEFT_FEED_FORWARD_OFFSET * signum(leftVelocity)) + velocityDelta

                val rightFeedForward = rightVelocity * RIGHT_FEED_FORWARD_COEFFICIENT +
                        (RIGHT_FEED_FORWARD_OFFSET * signum(rightVelocity)) - velocityDelta

                leftDrive.set(ControlMode.Position, leftDistance * ENCODER_TICKS_PER_METER,
                    DemandType.ArbitraryFeedForward, leftFeedForward)
                rightDrive.set(ControlMode.Position, rightDistance * ENCODER_TICKS_PER_METER,
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

    private fun logArcade(speed: Double, rotation: Double, quickTurn: Boolean? = null) {
        SmartDashboard.putNumber("Drivetrain Speed", speed)
        SmartDashboard.putNumber("Drivetrain Rotation", rotation)
        quickTurn?.let { SmartDashboard.putBoolean("Drivetrain Quick Turn", it) }
    }

    private fun logTank(left: Double, right: Double) {
        SmartDashboard.putNumber("Drivetrain Left Speed", left)
        SmartDashboard.putNumber("Drivetrain Right Speed", right)
    }
}
