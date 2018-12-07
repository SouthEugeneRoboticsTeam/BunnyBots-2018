package org.sert2521.bunnybots.dropper

import org.team2471.frc.lib.framework.use

suspend fun resetDroppers() = use(Dropper) {
    Dropper.reset()
}

suspend fun dropBunny() = use(Dropper, cancelConflicts = false) {
    Dropper.dropNext()
}
