package ch.zhdk.tracking.osc

import ch.zhdk.tracking.config.OscConfig
import com.illposed.osc.OSCPacket

abstract class OscChannel(val config : OscConfig) {
    abstract fun sendMessage(msg: OSCPacket)
}