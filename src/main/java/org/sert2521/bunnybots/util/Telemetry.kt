package org.sert2521.bunnybots.util

import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.loop
import org.team2471.frc.lib.framework.Subsystem
import kotlin.coroutines.CoroutineContext

object TelemetryScope : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}

class Telemetry {
    private data class Binding(val name: String, val body: () -> Any)

    private var telemetrySubsystem: Subsystem? = null
    private var telemetryName: String = ""

    private val bindings = mutableListOf<Binding>()
    val table = NetworkTableInstance.getDefault().getTable(telemetryName)!!

    constructor(name: String) {
        telemetryName = name
    }

    constructor(subsystem: Subsystem) {
        telemetrySubsystem = subsystem
        telemetryName = subsystem.name
    }

    private fun tick() = bindings.forEach { put(it.name, it.body()) }

    fun add(name: String, body: () -> Any) = bindings.add(Binding(name, body))

    fun remove(name: String) = bindings.filter { it.name == name }.forEach { bindings.remove(it) }

    fun put(name: String, value: Any) = table.getEntry(name).setValue(value)

    init {
        TelemetryScope.launch {
            loop(period = 0.1) {
                tick()
            }
        }
    }
}

val GlobalTelemetry = Telemetry("Global")

fun logBuildInfo() {
    println("\n-------------------- BUILD INFO --------------------")

    "branch.txt".asResource {
        println("Branch: $it")
        GlobalTelemetry.put("Branch", it)
    }

    "commit.txt".asResource {
        println("Commit: $it")
        GlobalTelemetry.put("Commit", it)
    }

    "changes.txt".asResource {
        println("Changes: [$it]")
        GlobalTelemetry.put("Changes", it)
    }

    "buildtime.txt".asResource {
        println("Build Time: $it")
        GlobalTelemetry.put("Build Time", it)
    }

    println("----------------------------------------------------\n")
}

fun String.asResource(body: (String) -> Unit) {
    val content = this.javaClass::class.java.getResource("/$this").readText()
    body(content)
}
