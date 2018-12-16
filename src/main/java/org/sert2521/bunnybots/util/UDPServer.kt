package org.sert2521.bunnybots.util

import com.google.gson.Gson
import org.sert2521.bunnybots.DEFAULT_JETSON_ADDRESS
import org.sert2521.bunnybots.UDP_PORT
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

enum class JetsonMessage(val message: String) {
    STOP("stop"),
    RUN("run")
}

object UDPServer : Thread() {
    private const val PACKET_SIZE = 128

    private val telemetry = Telemetry("Lidar")

    private val socket = DatagramSocket(UDP_PORT)
    private val gson = Gson()
    private var jetsonAddress: InetAddress = InetAddress.getByName(DEFAULT_JETSON_ADDRESS)

    fun send(message: String, recipient: InetAddress = jetsonAddress) {
        val messageData = message.toByteArray()

        val packet = DatagramPacket(messageData, messageData.size, recipient, UDP_PORT)
        socket.send(packet)
    }

    fun send(message: JetsonMessage, recipient: InetAddress = jetsonAddress) {
        val messageData = message.message.toByteArray()

        val packet = DatagramPacket(messageData, messageData.size, recipient, UDP_PORT)
        socket.send(packet)
    }

    override fun run() {
        while (true) {
            val buf = ByteArray(PACKET_SIZE)
            val packet = DatagramPacket(buf, buf.size)

            socket.receive(packet)
            val msg = String(packet.data).trim { it <= ' ' }

            if (socket.inetAddress != null) {
                jetsonAddress = socket.inetAddress
            }

            gson.fromJson(msg, LidarData::class.java).also {
                Lidar.apply {
                    if (it.alive == null) {
                        alive = true

                        when {
                            it.d != null -> {
                                distance = it.d.div(304.8) // mm -> ft
                                telemetry.put("Distance", distance!!)
                            }
                            else -> {
                                xOffset = it.x?.div(304.8) // mm -> ft
                                yOffset = it.y?.div(304.8) // mm -> ft
                                theta = it.t

                                xOffsets.removeAt(0)
                                xOffsets.add(xOffset ?: 0.0)

                                yOffsets.removeAt(0)
                                yOffsets.add(yOffset ?: 0.0)

                                thetas.removeAt(0)
                                thetas.add(theta ?: 0.0)

                                telemetry.put("Theta", theta ?: 0.0)
                                telemetry.put("X Offset", xOffset ?: 0.0)
                                telemetry.put("Y Offset", yOffset ?: 0.0)

                                telemetry.put("Theta Avg", thetaAverage)
                                telemetry.put("X Avg", xOffsetAverage)
                                telemetry.put("Y Avg", yOffsetAverage)
                            }
                        }
                    } else {
                        alive = it.alive
                    }

                    time = it.time
                    telemetry.put("Last Alive", time ?: -1)
                }
            }
        }
    }
}
