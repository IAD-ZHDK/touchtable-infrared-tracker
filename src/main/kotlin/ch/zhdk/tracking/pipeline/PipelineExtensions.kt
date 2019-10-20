package ch.zhdk.tracking.pipeline

import ch.bildspur.model.math.Float2
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import org.bytedeco.opencv.opencv_core.Point
import kotlin.math.roundToInt

fun ActiveRegion.toTactileObject(uniqueId : Int): TactileObject {
    val tactileObject = TactileObject(uniqueId)
    this.toTactileObject(tactileObject)
    return tactileObject
}

fun ActiveRegion.toTactileObject(tactileObject : TactileObject)
{
    tactileObject.position = this.center
    tactileObject.intensity = this.area
    tactileObject.detectionUpdatedTimeStamp = this.timestamp
    tactileObject.rotation = this.rotation
}

fun Float2.toPoint() : Point {
    return Point(this.x.roundToInt(), this.y.roundToInt())
}