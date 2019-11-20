package ch.zhdk.tracking.pipeline

import ch.bildspur.model.math.Float2
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import org.bytedeco.opencv.opencv_core.Point
import kotlin.math.roundToInt

fun ActiveRegion.toMarker(uniqueId : Int): Marker {
    val tactileObject = Marker(uniqueId)
    this.toMarker(tactileObject)
    return tactileObject
}

fun ActiveRegion.toMarker(marker : Marker)
{
    marker.position = this.center
    marker.intensity = this.area
    marker.detectionUpdatedTimeStamp = this.timestamp
}

fun Float2.toPoint() : Point {
    return Point(this.x.roundToInt(), this.y.roundToInt())
}