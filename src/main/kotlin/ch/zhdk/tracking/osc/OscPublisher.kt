package ch.zhdk.tracking.osc

import ch.zhdk.tracking.config.OscConfig
import ch.zhdk.tracking.model.TactileObject
import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.udp.OSCPort
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.net.InetSocketAddress

class OscPublisher(val config: OscConfig) {
    private lateinit var sender: OSCPortOut

    fun init(address: InetAddress, port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
        val target = InetSocketAddress(address, port)
        sender = OSCPortOut(target)
        println("sending OSC on  ${target.address}:${target.port}")
    }

    fun publish(tactileObjects: List<TactileObject>) {
        tactileObjects.forEach { publishObject(it) }
    }

    fun newObject(tactileObject: TactileObject) {
        val args = mutableListOf<Any>()
        args.add(tactileObject.uniqueId)
        args.add(tactileObject.calibratedPosition.x().toFloat())
        args.add(tactileObject.calibratedPosition.y().toFloat())
        args.add(tactileObject.rotation.toFloat())
        args.add(tactileObject.normalizedIntensity.toFloat())

        val msg = OSCMessage("/newID/${tactileObject.uniqueId}", args)
        sendMessage(msg)
    }

    fun removeObject(tactileObject: TactileObject) {
        val args = mutableListOf<Any>()
        args.add(tactileObject.uniqueId)

        val msg = OSCMessage("/untrack/${tactileObject.uniqueId}", args)
        sendMessage(msg)
    }

    private fun publishObject(tactileObject: TactileObject) {
        val args = mutableListOf<Any>()
        args.add(tactileObject.uniqueId)
        args.add(tactileObject.calibratedPosition.x().toFloat())
        args.add(tactileObject.calibratedPosition.y().toFloat())
        args.add(tactileObject.rotation.toFloat())
        args.add(tactileObject.normalizedIntensity.toFloat())

        if (config.debugOSC.value) {
            println("TO [${tactileObject.uniqueId}]: x: ${tactileObject.calibratedPosition.x().toFloat()} y: ${tactileObject.calibratedPosition.y().toFloat()}")
        }

        val msg = OSCMessage("/update/${tactileObject.uniqueId}", args)
        sendMessage(msg)
    }

    private fun sendMessage(msg: OSCMessage) {
        try {
            if (config.debugOSC.value) {
                println("Message: ${msg.address} (${msg.arguments.size})")
            }

            sender.send(msg)
        } catch (e: Exception) {
            println("Couldn't send osc message: ${e.message}")
        }
    }
}