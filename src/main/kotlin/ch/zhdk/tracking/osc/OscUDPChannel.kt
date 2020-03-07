package ch.zhdk.tracking.osc

import ch.zhdk.tracking.config.OscConfig
import com.illposed.osc.OSCPacket
import com.illposed.osc.transport.udp.OSCPort
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.net.InetSocketAddress

class OscUDPChannel(config : OscConfig) : OscChannel(config) {
    private lateinit var sender: OSCPortOut

    fun init(address: InetAddress, port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
        val target = InetSocketAddress(address, port)

        if(this::sender.isInitialized && sender.isConnected)
            sender.close()

        sender = OSCPortOut(target)
        println("starting OSC on  ${target.address}:${target.port}")
    }

    override fun sendMessage(msg: OSCPacket) {
        try {
            sender.send(msg)
        } catch (e: Exception) {
            println("Couldn't send osc message: ${e.message}")
        }
    }
}