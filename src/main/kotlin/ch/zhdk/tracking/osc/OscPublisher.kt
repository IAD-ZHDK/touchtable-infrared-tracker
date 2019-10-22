package ch.zhdk.tracking.osc

import ch.zhdk.tracking.config.OscConfig
import ch.zhdk.tracking.model.TactileObject
import com.illposed.osc.OSCBundle
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacket
import com.illposed.osc.transport.udp.OSCPort
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.net.InetSocketAddress

class OscPublisher(val config: OscConfig) {
    private lateinit var sender: OSCPortOut

    fun init(address: InetAddress, port: Int = OSCPort.DEFAULT_SC_OSC_PORT) {
        val target = InetSocketAddress(address, port)

        if(this::sender.isInitialized && sender.isConnected)
            sender.close()

        sender = OSCPortOut(target)
        println("starting OSC on  ${target.address}:${target.port}")
    }

    fun sendUpdate(tactileObjects: List<TactileObject>) {
        if(config.useOSCBundles.value) {
            sendBundledUpdate(tactileObjects)
            return
        }

        tactileObjects.forEach {
            if (config.debugOSC.value) {
                println("UPD [${it.uniqueId}]: x: ${it.calibratedPosition.x().toFloat()} y: ${it.calibratedPosition.y().toFloat()}")
            }

            sendMessage(createMessage("update", it.createArgsList()))
        }
    }

    private fun sendBundledUpdate(tactileObjects: List<TactileObject>) {
        val bundle = OSCBundle()

        tactileObjects.forEach {
            bundle.addPacket(createMessage("update", it.createArgsList()))
        }
        sendMessage(bundle)
    }

    fun newObject(tactileObject: TactileObject) {
        if (config.debugOSC.value) {
            println("ADD [${tactileObject.uniqueId}]: x: ${tactileObject.calibratedPosition.x().toFloat()} y: ${tactileObject.calibratedPosition.y().toFloat()}")
        }

        sendMessage(createMessage("add", tactileObject.createArgsList()))
    }

    fun removeObject(tactileObject: TactileObject) {
        if (config.debugOSC.value) {
            println("REM [${tactileObject.uniqueId}]: x: ${tactileObject.calibratedPosition.x().toFloat()} y: ${tactileObject.calibratedPosition.y().toFloat()}")
        }

        sendMessage(createMessage("remove", listOf(tactileObject.uniqueId)))
    }

    private fun createMessage(command : String, args : List<Any>) : OSCMessage {
        return OSCMessage("${config.nameSpace.value}/$command", args)
    }

    private fun sendMessage(msg: OSCPacket) {
        try {
            sender.send(msg)
        } catch (e: Exception) {
            println("Couldn't send osc message: ${e.message}")
        }
    }

    private fun TactileObject.createArgsList() : List<Any> {
        return listOf(
            this.uniqueId,
            this.identifier,
            this.calibratedPosition.x().toFloat(),
            this.calibratedPosition.y().toFloat(),
            this.rotation.toFloat(),
            this.normalizedIntensity.toFloat()
        )
    }
}