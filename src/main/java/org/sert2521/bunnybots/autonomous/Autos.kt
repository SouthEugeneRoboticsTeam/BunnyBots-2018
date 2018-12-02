package org.sert2521.bunnybots.autonomous

import org.sert2521.bunnybots.drivetrain.followPath
import org.team2471.frc.lib.motion_profiling.Path2D

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
