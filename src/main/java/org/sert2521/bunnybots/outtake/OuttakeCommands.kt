package org.sert2521.bunnybots.outtake

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun runOuttake() = use(Outtake) {
    periodic {
        Outtake.runBelt()
        Outtake.open()
    }
}
