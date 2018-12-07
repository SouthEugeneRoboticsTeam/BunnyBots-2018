package org.sert2521.bunnybots.autonomous

import edu.wpi.first.wpilibj.DigitalOutput
import org.sert2521.bunnybots.drivetrain.followPath
import org.sert2521.bunnybots.intake.runIntake
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.motion_profiling.Path2D

// LOW = keep red, HIGH = keep blue
val colorPin = DigitalOutput(2)

// LOW = let all in, HIGH = sort
val sortPin = DigitalOutput(3)

suspend fun runSelectedAuto() {
    parallel({ runIntake() }, {
        pickupAuto()

        val path = Path2D()
        path.addPointAngleAndMagnitude(0.0, 0.0, 0.0, 1.0)
        path.addPointAngleAndMagnitude(2.0, 2.0, -30.0, 1.0)
        path.speed = 3.0
        path.autonomous = autonomi["Tests"]

        println(path)

        followPath(path)
    })
}

suspend fun pickupAuto() {
    val auto = autonomi["BunnyBots"]
    auto.isMirrored = false

    val path = auto["Pickup"]

    try {
        followPath(path)
    } finally {
        println("Done following path")
    }
}

suspend fun testAuto() {
    val auto = autonomi["Tests"]
    auto.isMirrored = false

    val path = auto["8 Foot Straight"]
    path.robotDirection = Path2D.RobotDirection.FORWARD
    path.duration = 8.0

    try {
        followPath(path)
    } finally {
        println("Done following path")
    }
}
