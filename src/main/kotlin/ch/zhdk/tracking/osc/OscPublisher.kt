package ch.zhdk.tracking.osc

import ch.zhdk.tracking.config.OscConfig
import ch.zhdk.tracking.model.TactileDevice
import com.illposed.osc.OSCBundle
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCPacket

class OscPublisher(val config: OscConfig) {
    val channels = mutableListOf<OscChannel>()

    fun sendUpdate(devices: List<TactileDevice>) {
        if(config.useOSCBundles.value) {
            sendBundledUpdate(devices)
            return
        }

        devices.forEach {
            if (config.debugOSC.value) {
                println("OSC [${it.uniqueId}]: x: ${it.calibratedPosition.x().toFloat()} y: ${it.calibratedPosition.y().toFloat()}")
            }

            publishMessage(createMessage("update", it.createArgsList()))
        }
    }

    private fun sendBundledUpdate(devices: List<TactileDevice>) {
        val bundle = OSCBundle()

        devices.forEach {
            bundle.addPacket(createMessage("update", it.createArgsList()))
        }
        publishMessage(bundle)
    }

    fun newObject(device: TactileDevice) {
        if (config.debugOSC.value) {
            println("ADD [${device.uniqueId}]: x: ${device.calibratedPosition.x().toFloat()} y: ${device.calibratedPosition.y().toFloat()}")
        }

        publishMessage(createMessage("add", device.createArgsList()))
    }

    fun removeObject(device: TactileDevice) {
        if (config.debugOSC.value) {
            println("REM [${device.uniqueId}]: x: ${device.calibratedPosition.x().toFloat()} y: ${device.calibratedPosition.y().toFloat()}")
        }

        publishMessage(createMessage("remove", listOf(device.uniqueId)))
    }

    private fun createMessage(command : String, args : List<Any>) : OSCMessage {
        return OSCMessage("${config.nameSpace.value}/$command", args)
    }

    private fun publishMessage(msg: OSCPacket) {
        channels.forEach {
            it.sendMessage(msg)
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