package org.sert2521.bunnybots.util

import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.Preferences
import edu.wpi.first.wpilibj.XboxController
import org.sert2521.bunnybots.CONTROLLER_PORT
import org.sert2521.bunnybots.PRIMARY_STICK_PORT
import org.sert2521.bunnybots.SECONDARY_STICK_PORT
import org.sert2521.bunnybots.dropper.dropBunny
import org.sert2521.bunnybots.intake.runIntake
import org.sert2521.bunnybots.outtake.runOuttake
import org.team2471.frc.lib.framework.createMappings

val controller by lazy { XboxController(CONTROLLER_PORT) }
val primaryJoystick by lazy { Joystick(PRIMARY_STICK_PORT) }
val secondaryJoystick by lazy { Joystick(SECONDARY_STICK_PORT) }

val intakeSpeedScalar get() = Preferences.getInstance().getDouble("intake_speed_scalar", 0.8)
val normalEjectSpeedScalar
    get() = Preferences.getInstance().getDouble("normal_eject_speed_scalar", 0.6)
val fastEjectSpeedScalar get() = Preferences.getInstance().getDouble("fast_eject_speed_scalar", 1.0)
val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 0.85)

fun initControls() {
    primaryJoystick.createMappings {
        buttonHold(1) { runIntake() }
        buttonToggle(2) { runOuttake() }
        buttonPress(3) { dropBunny() }
    }
}

fun initPreferences() {
    Preferences.getInstance().putDouble("intake_speed_scalar", intakeSpeedScalar)
    Preferences.getInstance().putDouble("normal_eject_speed_scalar", normalEjectSpeedScalar)
    Preferences.getInstance().putDouble("fast_eject_speed_scalar", fastEjectSpeedScalar)
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)
}
