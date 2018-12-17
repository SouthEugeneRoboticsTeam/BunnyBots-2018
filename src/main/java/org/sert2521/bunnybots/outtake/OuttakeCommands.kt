package org.sert2521.bunnybots.outtake

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun runOuttake() = use(Outtake) {
    try {
        val timer = Timer().apply { start() }
        periodic {
            Outtake.run(-OUTTAKE_BELT_SPEED)

            if (timer.get() >= 0.25) exitPeriodic()
        }

        timer.stop()

        periodic {
            Outtake.run()
        }
    } finally {
        Outtake.stop()
    }
}

suspend fun toggleFlaps() = use(Outtake) {
    Outtake.toggle()
}
