package org.sert2521.bunnybots

import org.sert2521.bunnybots.arm.Arm
import org.sert2521.bunnybots.autonomous.AutoChooser
import org.sert2521.bunnybots.autonomous.auto
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.drivetrain.teleopDrive
import org.sert2521.bunnybots.util.UDPServer
import org.sert2521.bunnybots.util.initPreferences
import org.sert2521.bunnybots.util.logBuildInfo
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.disable
import org.team2471.frc.lib.framework.enable
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram
import org.team2471.frc.lib.motion_profiling.Path2D

object Robot : RobotProgram {
    init {
        AutoChooser

        Drivetrain
        Arm

        UDPServer.start()

        initPreferences()
        logBuildInfo()
    }

    private fun enableSubsystems() {
        Drivetrain.enable()
        Arm.enable()
    }

    private fun disableSubsystems() {
        Drivetrain.disable()
        Arm.disable()
    }

    override suspend fun teleop() {
        println("Entering teleop...")

        enableSubsystems()
        teleopDrive()

        println("Hello after teleopDrive!")
    }

    override suspend fun autonomous() {
        enableSubsystems()
        println("Entering autonomous...")
//        testAuto()

        println("Hello after auto!")

        val path = Path2D()

        path.addPointAngleAndMagnitude(0.0, 0.0, 0.0, 1.0)
        path.addPointAngleAndMagnitude(3.0, 3.0, 2.0, 1.0)

        path.addEasePoint(1.0, 0.25)
        path.addEasePoint(2.0, 1.0)

        path.name = "Test Path 123"
        path.duration = 5.0
        path.autonomous = auto
        path.robotDirection = Path2D.RobotDirection.BACKWARD

        Drivetrain.driveAlongPath(path ?: Path2D(), 1.0)
    }

    override suspend fun test() {
//        enableSubsystems()
        println("Entering test...")
    }

    override suspend fun disable() {
        println("Entering disable...")
        disableSubsystems()
    }
}

fun main(args: Array<String>) {
    initializeWpilib()

    runRobotProgram(Robot)
}
