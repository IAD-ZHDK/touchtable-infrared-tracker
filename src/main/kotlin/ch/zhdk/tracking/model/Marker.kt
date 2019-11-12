package ch.zhdk.tracking.model

import ch.bildspur.util.TimeKeeper
import ch.zhdk.tracking.model.identification.Identification
import ch.zhdk.tracking.model.state.MarkerState
import org.bytedeco.opencv.opencv_core.Point2d

class Marker(val uniqueId: Int) {
    var identifier = -1
    var position = Point2d()
    var rotation = 0.0

    // todo: maybe just calculate on the fly
    var normalizedPosition = Point2d()
    var normalizedIntensity: Double = 0.0
    var calibratedPosition = Point2d()

    var state = MarkerState.Detected
    var stateChangeTimeStamp = TimeKeeper.millis()
        private set

    // last updated by active region (frame timestamp)
    var detectionUpdatedTimeStamp = 0L

    // tracking relevant
    var matchedWithRegion = false

    // intensity detected by active region
    var intensity = 0.0

    // identification
    // todo: move this property into tactile object
    val identification = Identification()

    // methods
    fun updateState(state: MarkerState) {
        if (this.state == state)
            return

        stateChangeTimeStamp = TimeKeeper.millis()
        this.state = state
    }

    val timeSinceLastStateChange: Long
        get() = TimeKeeper.millis() - stateChangeTimeStamp

    val timeSinceLastDetectionUpdate: Long
        get() = TimeKeeper.millis() - detectionUpdatedTimeStamp

    val isActive: Boolean
        get() = state == MarkerState.Alive || state == MarkerState.Missing
}