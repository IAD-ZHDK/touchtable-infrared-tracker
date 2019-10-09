package ch.zhdk.tracking.osc

import ch.zhdk.tracking.model.TactileObject
import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.udp.OSCPort
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.net.InetSocketAddress

class OscPublisher {
    private lateinit var sender : OSCPortOut

    fun init(address : InetAddress, port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
        val target = InetSocketAddress(address, port)
        sender = OSCPortOut(target)
        println("sending OSC on  ${target.address}:${target.port}")
    }

    fun publish(tactileObjects: List<TactileObject>) {
        tactileObjects.forEach { publishObject(it) }
    }

    private fun publishObject(tactileObject: TactileObject) {
        val args = mutableListOf<Any>()
        args.add(tactileObject.uniqueId)
        args.add(tactileObject.identifier)
        args.add(tactileObject.normalizedPosition.x().toFloat())
        args.add(tactileObject.normalizedPosition.y().toFloat())
        args.add(tactileObject.rotation.toFloat())
        args.add(tactileObject.normalizedIntensity.toFloat())

        val msg = OSCMessage("/ir/object", args)

        try {
            sender.send(msg)
        } catch (e: Exception) {
            println("Couldn't send osc message: ${e.message}")
        }

    }
}