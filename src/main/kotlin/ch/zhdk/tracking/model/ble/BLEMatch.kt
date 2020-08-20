package ch.zhdk.tracking.model.ble

import ch.broox.ble.BLEDevice
import ch.zhdk.tracking.model.TactileDevice

class BLEMatch(val bleDevice : BLEDevice, var tactileDevice: TactileDevice? = null) {
    var lastUpdateTimestamp = 0L

    val matched : Boolean
        get() = tactileDevice != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BLEMatch

        if (bleDevice != other.bleDevice) return false

        return true
    }

    override fun hashCode(): Int {
        return bleDevice.hashCode()
    }
}