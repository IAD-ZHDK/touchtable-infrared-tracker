package ch.zhdk.tracking.osc

import ch.bildspur.timer.Timer
import ch.zhdk.tracking.model.TactileObject
import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.udp.OSCPort
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.net.InetSocketAddress

class OscPublisher(port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
    private val target = InetAddress.getByName("192.168.1.7")
    private var sender = OSCPortOut(InetSocketAddress(target, port))

    init {
        println("sending to interface: ${sender.remoteAddress}")

    }

    fun init(port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
        sender.close()
        sender = OSCPortOut(InetSocketAddress(target, port))
    }

    fun publish(tactileObjects: List<TactileObject>) {
        tactileObjects.forEach { publishObject(it) }
    }

    private fun publishObject(tactileObject: TactileObject) {
        val args = mutableListOf<Any>()
        //args.add(tactileObject.uniqueId)
        args.add(1)
        args.add(tactileObject.identifier)
        args.add(tactileObject.normalizedPosition.x().toFloat())
        args.add(tactileObject.normalizedPosition.y().toFloat())
        args.add(30f) // rotation
        args.add(tactileObject.normalizedIntensity.toFloat())

        val msg = OSCMessage("/newID", args)

        try {
            sender.send(msg)
        } catch (e: Exception) {
            println("Couldn't send osc message: ${e.message}")
        }

    }
}