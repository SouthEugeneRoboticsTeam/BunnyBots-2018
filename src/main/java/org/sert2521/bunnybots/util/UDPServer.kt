package org.sert2521.bunnybots.util

import com.google.gson.Gson
import java.net.DatagramPacket
import java.net.DatagramSocket

object UDPServer : Thread() {
    private const val PACKET_SIZE = 128

    private val socket = DatagramSocket(UDP_PORT)
    private val gson = Gson()

    val telemetry = Telemetry("Lidar")

    override fun run() {
        while (true) {
            val buf = ByteArray(PACKET_SIZE)
            val packet = DatagramPacket(buf, buf.size)

            socket.receive(packet)
            val msg = String(packet.data).trim { it <= ' ' }

            gson.fromJson(msg, LidarData::class.java).also {
                Lidar.apply {
                    if (it.alive == null) {
                        xOffset = it.xOffset
                        yOffset = it.yOffset
                        theta = it.theta

                        telemetry.put("Theta", theta ?: 0.0)
                        telemetry.put("X Offset", xOffset ?: 0.0)
                        telemetry.put("Y Offset", yOffset ?: 0.0)
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
