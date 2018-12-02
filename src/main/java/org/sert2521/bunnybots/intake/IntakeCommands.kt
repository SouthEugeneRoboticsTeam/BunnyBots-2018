package org.sert2521.bunnybots.intake

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun runIntake() = use(Intake) {
    periodic {
        Intake.runIntake()
    }
}
