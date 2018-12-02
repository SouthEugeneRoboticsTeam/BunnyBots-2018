package org.sert2521.bunnybots

import org.sert2521.bunnybots.arm.Arm
import org.sert2521.bunnybots.autonomous.AutoChooser
import org.sert2521.bunnybots.autonomous.testAuto
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.drivetrain.teleopDrive
import org.sert2521.bunnybots.dropper.Dropper
import org.sert2521.bunnybots.dropper.resetDroppers
import org.sert2521.bunnybots.util.UDPServer
import org.sert2521.bunnybots.util.initControls
import org.sert2521.bunnybots.util.initPreferences
import org.sert2521.bunnybots.util.logBuildInfo
import org.team2471.frc.lib.framework.RobotProgram
import org.team2471.frc.lib.framework.disable
import org.team2471.frc.lib.framework.enable
import org.team2471.frc.lib.framework.initializeWpilib
import org.team2471.frc.lib.framework.runRobotProgram

object Robot : RobotProgram {
    init {
        AutoChooser

        Drivetrain
        Arm
        Dropper

        UDPServer.start()

        initControls()
        initPreferences()
        logBuildInfo()
    }

    private suspend fun initCommands() {
        resetDroppers()
    }

    private suspend fun enableSubsystems() {
        Drivetrain.enable()
        Arm.enable()

        initCommands()
    }

    private fun disableSubsystems() {
        Drivetrain.disable()
        Arm.disable()
    }

    override suspend fun teleop() {
        println("Entering teleop...")

        enableSubsystems()
        teleopDrive()
    }

    override suspend fun autonomous() {
        println("Entering autonomous...")

        enableSubsystems()
        testAuto()
    }

    override suspend fun test() {
        println("Entering test...")

        enableSubsystems()
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
