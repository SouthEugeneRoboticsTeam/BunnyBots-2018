package org.sert2521.bunnybots

import edu.wpi.first.wpilibj.DriverStation
import org.sert2521.bunnybots.arm.Arm
import org.sert2521.bunnybots.autonomous.AutoChooser
import org.sert2521.bunnybots.autonomous.colorPin
import org.sert2521.bunnybots.autonomous.runSelectedAuto
import org.sert2521.bunnybots.autonomous.sortPin
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.drivetrain.teleopDrive
import org.sert2521.bunnybots.dropper.Dropper
import org.sert2521.bunnybots.dropper.resetDroppers
import org.sert2521.bunnybots.intake.Intake
import org.sert2521.bunnybots.outtake.Outtake
import org.sert2521.bunnybots.util.JetsonMessage
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
        Intake
        Outtake

//        CameraServer.getInstance().startAutomaticCapture(0).apply {
//            setFPS(15)
//            setResolution(320, 240)
//        }
//        CameraServer.getInstance().startAutomaticCapture(1).apply {
//            setFPS(15)
//            setResolution(320, 240)
//        }

        UDPServer.start()

        initControls()
        initPreferences()
        logBuildInfo()
    }

    private fun enableSubsystems() {
        colorPin.set(DriverStation.getInstance().alliance == DriverStation.Alliance.Red)

        Drivetrain.enable()
        Arm.enable()
        Dropper.enable()
        Intake.enable()
        Outtake.enable()
    }

    private fun disableSubsystems() {
        Drivetrain.disable()
        Arm.disable()
        Dropper.disable()
        Intake.disable()
        Outtake.disable()
    }

    override suspend fun enable() {
        enableSubsystems()
        resetDroppers()
    }

    override suspend fun disable() {
        disableSubsystems()
    }

    override suspend fun teleop() {
        println("Entering teleop...")

        sortPin.set(true)

        Outtake.openTeleop()
        teleopDrive()
    }

    override suspend fun autonomous() {
        println("Entering autonomous...")

        sortPin.set(false)

        try {
            UDPServer.send(JetsonMessage.RUN)

            runSelectedAuto()
        } finally {
            UDPServer.send(JetsonMessage.STOP)
        }
    }
}

fun main() {
    initializeWpilib()

    runRobotProgram(Robot)
}
