package org.sert2521.bunnybots

import org.sert2521.bunnybots.arm.Arm
import org.sert2521.bunnybots.autonomous.AutoChooser
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.util.UDPServer
import org.sert2521.bunnybots.util.initPreferences
import org.sert2521.bunnybots.util.logBuildInfo
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram
import org.team2471.frc.lib.util.Environment
import org.team2471.frc.lib.util.RuntimeType

object Robot : RobotProgram {
    init {
        // Ensure we're running on a real robot
        if (Environment.runtimeType == RuntimeType.REAL) {
            AutoChooser

            Drivetrain
            Arm
        } else {
            println("Beginning limited simulation...")
        }

        UDPServer.start()

        initPreferences()
        logBuildInfo()
    }

    override suspend fun teleop() {
        println("Entering teleop...")
//        teleopDrive()
    }

    override suspend fun autonomous() {
        println("Entering autonomous...")
//        testAuto()

//        val path = Path2D()
//
//        path.addPointAngleAndMagnitude(1.0, 1.0, 45.0, 1.0)
//        path.addPointAngleAndMagnitude(0.0, 0.0, 0.0, 1.0)
//
//        path.duration = 2.0
//
//        runBlocking {
//            Drivetrain.driveAlongPath(path)
//        }
    }

    override suspend fun test() {
        println("Entering test...")
    }

    override suspend fun disable() {
        println("Entering disable...")
    }
}

fun main() {
    initializeWpilib()

    runRobotProgram(Robot)
}
