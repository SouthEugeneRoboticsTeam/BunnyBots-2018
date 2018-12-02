package org.sert2521.bunnybots.autonomous

import edu.wpi.first.networktables.EntryListenerFlags
import edu.wpi.first.networktables.EntryNotification
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import org.sert2521.bunnybots.drivetrain.Drivetrain
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.motion_profiling.Autonomi
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.util.measureTimeFPGA
import java.io.File

private lateinit var autonomi: Autonomi
val auto get() = autonomi["BunnyBots"]

enum class Side {
    LEFT,
    RIGHT,
    CENTER,
}

object AutoChooser {
    private val cacheFile = File("/home/lvuser/autonomi.json")

    init {
        try {
            autonomi = Autonomi.fromJsonString(cacheFile.readText())
            println("Autonomi cache loaded.")
        } catch (_: Exception) {
            DriverStation.reportError("Autonomi cache could not be found", false)
            autonomi = Autonomi()
        } finally {
            println("Done dealing with cache...")
        }

        val handler = { event: EntryNotification ->
            val json = event.value.string
            println("Got JSON: $json")
            if (!json.isEmpty()) {
                val t = measureTimeFPGA {
                    autonomi = Autonomi.fromJsonString(json)
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

suspend fun testAuto() = use(Drivetrain) {
    val auto = autonomi["Tests"]
    auto.isMirrored = false

    val path = auto["8 Foot Straight"]
    path.robotDirection = Path2D.RobotDirection.FORWARD
    path.duration = 8.0

    try {
        Drivetrain.driveAlongPath(path)
    } finally {
        println("Done following path")
    }
}
