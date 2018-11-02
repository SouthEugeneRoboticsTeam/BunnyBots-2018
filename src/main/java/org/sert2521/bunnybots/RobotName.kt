package org.sert2521.bunnybots

import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.util.initPreferences
import org.sert2521.bunnybots.util.logTelemetry
import org.sertain.Robot

class RobotName : Robot() {
    override fun onCreate() {
//        AutoChooser

        Drivetrain
//        Arm

//        UDPServer.start()
//        CameraServer.getInstance().startAutomaticCapture()

        initPreferences()
        logTelemetry()
    }

//    override fun onAutoStart() = driveStraightAuto.launch()
    override fun executeAuto() {
        Drivetrain.drive(0.6, 0.0)
//        launch {
//            Drivetrain.drivePosition(10*4069.0, 10*4069.0)
//        }
    }

    override fun executeTeleop() {
        Drivetrain.drive(0.0, 0.6)
//        talon.drive(0.6, 0.0)
//        Arm.armMotor.set(ControlMode.Velocity, 500.0)
    }
}
