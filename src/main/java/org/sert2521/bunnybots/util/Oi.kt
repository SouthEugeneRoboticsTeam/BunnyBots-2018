package org.sert2521.bunnybots.util

import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.Joystick
import edu.wpi.first.wpilibj.Preferences
import org.sert2521.bunnybots.PRIMARY_STICK_PORT
import org.sert2521.bunnybots.SECONDARY_STICK_PORT
import org.sert2521.bunnybots.arm.ArmPose
import org.sert2521.bunnybots.arm.animateArmToPose
import org.sert2521.bunnybots.dropper.dropBunny
import org.sert2521.bunnybots.intake.reverseIntake
import org.sert2521.bunnybots.intake.runIntake
import org.sert2521.bunnybots.outtake.runOuttake
import org.sert2521.bunnybots.outtake.toggleFlaps
import org.team2471.frc.lib.framework.createMappings

// LOW = keep red, HIGH = keep blue
val colorPin = DigitalOutput(2)

// LOW = let all in, HIGH = sort
val sortPin = DigitalOutput(3)

val primaryJoystick by lazy { Joystick(PRIMARY_STICK_PORT) }
val secondaryJoystick by lazy { Joystick(SECONDARY_STICK_PORT) }

val driveSpeedScalar get() = Preferences.getInstance().getDouble("drive_speed_scalar", 1.0)

fun initControls() {
    primaryJoystick.createMappings {
        buttonHold(3) { animateArmToPose(ArmPose.BOTTOM) }
    }

    secondaryJoystick.createMappings {
        buttonPress(4) { toggleFlaps() }
        buttonPress(7) { dropBunny() }
        buttonToggle(12) { runIntake() }
        buttonHold(3) { runOuttake() }
        buttonHold(11) { reverseIntake() }
    }
}

fun initPreferences() {
    Preferences.getInstance().putDouble("drive_speed_scalar", driveSpeedScalar)
}
