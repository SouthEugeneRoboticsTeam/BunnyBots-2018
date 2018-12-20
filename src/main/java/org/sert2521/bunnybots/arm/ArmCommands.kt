package org.sert2521.bunnybots.arm

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun animateArmToPose(pose: ArmPose) = use(Arm) {
    try {
        periodic {
//            Arm.setPose(pose)
            println("Running:")
            println(Arm.position)
            if (Math.abs(Arm.position - pose.armPosition) < 40) exitPeriodic()

            println("${Arm.position}, ${pose.armPosition}")
            if (Arm.position < pose.armPosition) Arm.setPercent(0.4)
            else Arm.setPercent(-0.4)
        }
    } finally {
        Arm.stop()
    }
}
