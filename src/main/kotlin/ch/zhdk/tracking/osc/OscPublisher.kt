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

        if(this::sender.isInitialized && sender.isConnected)
            sender.close()

        sender = OSCPortOut(target)
        println("starting OSC on  ${target.address}:${target.port}")
    }

    fun sendUpdate(tactileObjects: List<TactileObject>) {
        tactileObjects.forEach {
            if (config.debugOSC.value) {
                println("UPD [${it.uniqueId}]: x: ${it.calibratedPosition.x().toFloat()} y: ${it.calibratedPosition.y().toFloat()}")
            }

            sendMessage("update", it.createArgsList())
        }
    }

    fun newObject(tactileObject: TactileObject) {
        if (config.debugOSC.value) {
            println("ADD [${tactileObject.uniqueId}]: x: ${tactileObject.calibratedPosition.x().toFloat()} y: ${tactileObject.calibratedPosition.y().toFloat()}")
        }

        sendMessage("add", tactileObject.createArgsList())
    }

    fun removeObject(tactileObject: TactileObject) {
        if (config.debugOSC.value) {
            println("REM [${tactileObject.uniqueId}]: x: ${tactileObject.calibratedPosition.x().toFloat()} y: ${tactileObject.calibratedPosition.y().toFloat()}")
        }

        sendMessage("remove", listOf(tactileObject.uniqueId))
    }

    private fun sendMessage(command : String, args : List<Any>) {
        sendMessage(OSCMessage("${config.nameSpace.value}/$command", args))
    }

    private fun sendMessage(msg: OSCMessage) {
        try {
            sender.send(msg)
        } catch (e: Exception) {
            println("Couldn't send osc message: ${e.message}")
        }
    }

    private fun TactileObject.createArgsList() : List<Any> {
        return listOf(
            this.uniqueId,
            this.calibratedPosition.x().toFloat(),
            this.calibratedPosition.y().toFloat(),
            this.rotation.toFloat(),
            this.normalizedIntensity.toFloat()
        )
    }
}