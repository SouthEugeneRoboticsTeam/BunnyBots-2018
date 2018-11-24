package org.sert2521.bunnybots.util

import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.Preferences
import edu.wpi.first.wpilibj.XboxController
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

// Driver controller
val controller by lazy { XboxController(CONTROLLER_PORT) }

// Gunner joystick
val secondaryJoystick by lazy { Joystick(SECONDARY_STICK_PORT) }

val intakeSpeedScalar get() = Preferences.getInstance().getDouble("intake_speed_scalar", 0.8)
val normalEjectSpeedScalar
    get() = Preferences.getInstance().getDouble("normal_eject_speed_scalar", 0.6)
val fastEjectSpeedScalar get() = Preferences.getInstance().getDouble("fast_eject_speed_scalar", 1.0)
val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 0.85)

fun initPreferences() {
    Preferences.getInstance().putDouble("intake_speed_scalar", intakeSpeedScalar)
    Preferences.getInstance().putDouble("normal_eject_speed_scalar", normalEjectSpeedScalar)
    Preferences.getInstance().putDouble("fast_eject_speed_scalar", fastEjectSpeedScalar)
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)

    SmartDashboard.putNumber("Drive kP", SmartDashboard.getNumber("Drive kP", 1.0))
    SmartDashboard.putNumber("Drive kD", SmartDashboard.getNumber("Drive kD", 0.0))
    SmartDashboard.setPersistent("Drive kP")
    SmartDashboard.setPersistent("Drive kD")
}
