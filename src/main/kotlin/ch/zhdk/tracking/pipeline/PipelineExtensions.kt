package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject

fun ActiveRegion.toTactileObject(uniqueId : Int): TactileObject {
    val tactileObject = TactileObject(uniqueId)
    this.toTactileObject(tactileObject)
    return tactileObject
}

fun ActiveRegion.toTactileObject(tactileObject : TactileObject)
{
    tactileObject.position = this.center
    tactileObject.currentIntensity = this.area
    tactileObject.lastUpdateTimestamp = this.timestamp
}