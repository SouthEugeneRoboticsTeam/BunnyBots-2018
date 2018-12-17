package org.sert2521.bunnybots.autonomous

import edu.wpi.first.wpilibj.DigitalOutput
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.drivetrain.followPath
import org.sert2521.bunnybots.intake.Intake
import org.sert2521.bunnybots.intake.runIntake
import org.sert2521.bunnybots.outtake.Outtake
import org.sert2521.bunnybots.outtake.runOuttake
import org.sert2521.bunnybots.util.Lidar
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.vector.Vector2
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin

// LOW = keep red, HIGH = keep blue
val colorPin = DigitalOutput(2)

// LOW = let all in, HIGH = sort
val sortPin = DigitalOutput(3)

suspend fun runSelectedAuto() {
    Outtake.closeAuto()

    try {
        parallel({ runIntake() }, {
            pickupPath()
            endToCratesPath()
            parallel({
                delay(1.35)
                Outtake.openAuto()
                runOuttake()
            }, {
                driveParallelToCrates()
                driveParallelToCrates(forward = false)
            })
        })
    } finally {
        println("In finally")
        Drivetrain.stop()
        Outtake.stop()
        Intake.stop()

        Outtake.closeAuto()
    }
}

suspend fun pickupPath() {
    val auto = autonomi["BunnyBots"]

    val path = auto["Pickup"]

    try {
        followPath(path)
    } finally {
        println("Done following path")
    }
}

suspend fun driveParallelToCrates(forward: Boolean = true) {
    val auto = autonomi["BunnyBots"]

    val path = auto["Crates"]
    path.robotDirection = if (forward) {
        Path2D.RobotDirection.FORWARD
    } else {
        Path2D.RobotDirection.BACKWARD
    }

    try {
        followPath(path, useLidar = true, forward = forward)
    } finally {
        println("Done following path")
    }
}

suspend fun endToCratesPath() {
    val auto = autonomi["BunnyBots"]

    val path = Path2D()
    path.autonomous = auto

    val angle = Lidar.theta ?: 0.0
    val xOffset = 29.0 / 12.0
    val yOffset = 20.0 / 12.0

    // Calculate tangents given angle
    val magnitude = 4.5
    val tangentX = cos(toRadians(angle + 90.0)) * magnitude * -1
    val tangentY = sin(toRadians(angle + 90.0)) * magnitude

    // Rotate coordinate plane to align with robot angle
    // See: https://en.wikipedia.org/wiki/Rotation_of_axes
    val endpoint = Vector2(-1 * (Lidar.xOffset ?: 0.0) + xOffset, (Lidar.yOffset ?: 0.0) - yOffset).apply {
        rotateRadians(toRadians(-angle))
    }

    path.addPointAndTangent(0.0, 0.0, 0.0, 1.25 * 3)
    path.addPointAndTangent(endpoint.x, endpoint.y, tangentX, tangentY)

    // Use linear ease line, e.g. time is directly proportional to % path completed
    path.addEasePoint(0.0, 0.0)
    path.addEasePoint(1.0, 1.0)

    path.duration = 2.0

    try {
        followPath(path, 0.25)
    } finally {
        println("Done following path")
    }
}

suspend fun testAuto() {
    val auto = autonomi["Tests"]
    auto.isMirrored = false

    val path = auto["8 Foot Straight"]
    path.robotDirection = Path2D.RobotDirection.FORWARD
    path.duration = 3.0

    try {
        followPath(path, 0.5)
    } finally {
        println("Done following path")
    }
}
