package org.sert2521.bunnybots.intake

import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.use

suspend fun runIntake() = use(Intake) {
    try {
        periodic {
            Intake.run()
        }
    } finally {
        Intake.stop()
    }
}

suspend fun reverseIntake() = use(Intake) {
    try {
        periodic {
            Intake.reverse()
        }
    } finally {
        Intake.stop()
    }
}