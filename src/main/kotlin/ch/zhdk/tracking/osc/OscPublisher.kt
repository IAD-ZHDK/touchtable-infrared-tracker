package ch.zhdk.tracking.osc

import ch.zhdk.tracking.config.OscConfig
import ch.zhdk.tracking.model.TactileDevice
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

    fun sendUpdate(devices: List<TactileDevice>) {
        if(config.useOSCBundles.value) {
            sendBundledUpdate(devices)
            return
        }

        devices.forEach {
            if (config.debugOSC.value) {
                println("UPD [${it.uniqueId}]: x: ${it.calibratedPosition.x().toFloat()} y: ${it.calibratedPosition.y().toFloat()}")
            }

            sendMessage(createMessage("update", it.createArgsList()))
        }
    }

    private fun sendBundledUpdate(devices: List<TactileDevice>) {
        val bundle = OSCBundle()

        devices.forEach {
            bundle.addPacket(createMessage("update", it.createArgsList()))
        }
        sendMessage(bundle)
    }

    fun newObject(device: TactileDevice) {
        if (config.debugOSC.value) {
            println("ADD [${device.uniqueId}]: x: ${device.calibratedPosition.x().toFloat()} y: ${device.calibratedPosition.y().toFloat()}")
        }

        sendMessage(createMessage("add", device.createArgsList()))
    }

    fun removeObject(device: TactileDevice) {
        if (config.debugOSC.value) {
            println("REM [${device.uniqueId}]: x: ${device.calibratedPosition.x().toFloat()} y: ${device.calibratedPosition.y().toFloat()}")
        }

        sendMessage(createMessage("remove", listOf(device.uniqueId)))
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

    private fun TactileDevice.createArgsList() : List<Any> {
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