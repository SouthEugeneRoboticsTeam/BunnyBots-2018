package org.sert2521.bunnybots

import edu.wpi.first.wpilibj.CameraServer
import org.sert2521.bunnybots.autonomous.driveStraightAuto
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.sert2521.bunnybots.util.UDPServer
import org.sert2521.bunnybots.util.initPreferences
import org.sert2521.bunnybots.util.logTelemetry
import org.sertain.Robot

class RobotName : Robot() {
    override fun onCreate() {
        Drivetrain

        UDPServer.start()
        CameraServer.getInstance().startAutomaticCapture()

        initPreferences()
        logTelemetry()
    }

    override fun onAutoStart() = driveStraightAuto.launch()
}
