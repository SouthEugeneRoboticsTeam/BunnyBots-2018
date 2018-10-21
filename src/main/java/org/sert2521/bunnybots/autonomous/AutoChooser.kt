package org.sert2521.bunnybots.autonomous

import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.EntryNotification
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.team2471.frc.lib.control.experimental.Command
import org.team2471.frc.lib.motion_profiling.Autonomi
import org.team2471.frc.lib.util.measureTimeFPGA
import java.io.File

private lateinit var autonomi: Autonomi

enum class Side {
    LEFT,
    RIGHT,
    CENTER,
}

object AutoChooser {
    private val cacheFile = File("/home/lvuser/autonomi.json")

    init {
        val handler = { event: EntryNotification ->
            val json = event.value.string
            if (!json.isEmpty()) {
                val t = measureTimeFPGA {
                    autonomi =
                            Autonomi.fromJsonString(json)
                }
                println("Loaded autonomi in $t seconds")

                cacheFile.writeText(json)
                println("New autonomi written to cache")
            } else {
                autonomi = Autonomi()
                DriverStation.reportWarning("Empty autonomi received from network tables",
                                            false)
            }
        }

        val flags = EntryListenerFlags.kImmediate or
                EntryListenerFlags.kNew or
                EntryListenerFlags.kUpdate

        NetworkTableInstance.getDefault()
            .getTable("PathVisualizer")
            .getEntry("Autonomi")
            .addListener(handler, flags)
    }

    private val sideChooser = SendableChooser<Side>().apply {
        addDefault("Left", Side.LEFT)
        addObject("Center", Side.CENTER)
        addObject("Right", Side.RIGHT)
    }
}

val driveStraightAuto = Command("Drive Straight", Drivetrain) {
    val auto = autonomi["Tests"]
    auto.isMirrored = false
    try {
        Drivetrain.driveAlongPath(auto["4 Foot Straight"])
    } finally {
        println("Done following path")
    }
}
