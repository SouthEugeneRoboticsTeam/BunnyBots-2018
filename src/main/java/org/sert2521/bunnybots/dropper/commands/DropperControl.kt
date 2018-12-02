package org.sert2521.bunnybots.dropper.commands

import org.sert2521.bunnybots.dropper.Dropper
import org.sertain.command.Command

class DropperControl : Command() {
    init {
        requires(Dropper)
    }

    override fun execute(): Boolean {
        return false
    }
}
