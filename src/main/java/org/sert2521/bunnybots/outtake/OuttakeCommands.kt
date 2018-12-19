package org.sert2521.bunnybots.outtake

import org.sert2521.bunnybots.util.timedPeriodic
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun runOuttake() = use(Outtake) {
    try {
        timedPeriodic(time = 0.25) {
            Outtake.run(-OUTTAKE_BELT_SPEED)
        }

        periodic {
            Outtake.run()
        }
    } finally {
        Outtake.stop()
    }
}

suspend fun runTeleopOuttake() = use(Outtake) {
    try {
        Outtake.openTeleop()
        runOuttake()
    } finally {
        Outtake.closeTeleop()
    }
}

suspend fun runAutoOuttake() = use(Outtake) {
    try {
        Outtake.openAuto()
        runOuttake()
    } finally {
        Outtake.closeAuto()
    }
}
