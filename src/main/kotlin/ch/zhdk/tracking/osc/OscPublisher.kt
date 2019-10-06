package ch.zhdk.tracking.osc

import ch.bildspur.timer.Timer
import ch.zhdk.tracking.model.TactileObject
import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.udp.OSCPort
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.net.InetSocketAddress

class OscPublisher(port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
    private var sender = OSCPortOut(InetSocketAddress(InetAddress.getLocalHost(), port))

    fun init(port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
        sender.close()
        sender = OSCPortOut(InetSocketAddress(InetAddress.getLocalHost(), port))
    }

    fun publish(tactileObjects: List<TactileObject>) {
        tactileObjects.forEach { publishObject(it) }
    }

    private fun publishObject(tactileObject: TactileObject) {
        val args = mutableListOf<Any>()
        args.add(tactileObject.uniqueId)
        args.add(tactileObject.identifier)
        args.add(tactileObject.position.x())
        args.add(tactileObject.position.y())
        args.add(tactileObject.lifeTime)

        val msg = OSCMessage("/ir/object", args)

        try {
            sender.send(msg)
        } catch (e: Exception) {
            println("Couldn't send osc message!")
        }

    }
}