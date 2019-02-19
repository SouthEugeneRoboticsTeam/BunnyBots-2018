package org.sert2521.bunnybots

import edu.wpi.first.wpilibj.CameraServer
import edu.wpi.first.wpilibj.DriverStation
import kotlinx.coroutines.launch
import org.sert2521.bunnybots.arm.Arm
import org.sert2521.bunnybots.arm.ArmPose
import org.sert2521.bunnybots.arm.animateArmToPose
import org.sert2521.bunnybots.autonomous.AutoChooser
import org.sert2521.bunnybots.autonomous.runSelectedAuto
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.drivetrain.teleopDrive
import org.sert2521.bunnybots.dropper.Dropper
import org.sert2521.bunnybots.dropper.resetDroppers
import org.sert2521.bunnybots.intake.Intake
import org.sert2521.bunnybots.outtake.Outtake
import org.sert2521.bunnybots.util.colorPin
import org.sert2521.bunnybots.util.initControls
import org.sert2521.bunnybots.util.initPreferences
import org.sert2521.bunnybots.util.logBuildInfo
import org.sert2521.bunnybots.util.sortPin
import org.team2471.frc.lib.coroutines.MeanlibScope
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.RobotProgram
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

        CameraServer.getInstance().startAutomaticCapture("Intake Stream", 0).apply {
            setFPS(15)
        }
        CameraServer.getInstance().startAutomaticCapture("Outtake Stream", 1).apply {
            setFPS(15)
        }

        initControls()
        initPreferences()
        logBuildInfo()
    }

    override suspend fun enable() {
        colorPin.set(DriverStation.getInstance().alliance == DriverStation.Alliance.Red)

        Outtake.closeAuto()
        Outtake.closeTeleop()
        resetDroppers()
    }

    override suspend fun disable() {
        Drivetrain.stop()
        Outtake.stop()
        Intake.stop()
    }

    override suspend fun teleop() {
        println("Entering teleop...")

        disable()
      
        sortPin.set(true)

        animateArmToPose(ArmPose.TOP)
        Outtake.openTeleop()

        teleopDrive()
    }

    override suspend fun autonomous() {
        println("Entering autonomous...")

        sortPin.set(false)

        runSelectedAuto()
    }
}

fun main() {
    initializeWpilib()

    runRobotProgram(Robot)
}
