package org.sert2521.bunnybots.outtake

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun runOuttake() = use(Outtake) {
    try {
        periodic {
            Outtake.run()
        }
    } finally {
        Outtake.stop()
    }
}

suspend fun openFlap() = use(Outtake) {
    Outtake.open(true)
}
