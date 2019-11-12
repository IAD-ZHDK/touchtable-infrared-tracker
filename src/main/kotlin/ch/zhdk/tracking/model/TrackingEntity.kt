package ch.zhdk.tracking.model

import ch.bildspur.util.TimeKeeper
import ch.zhdk.tracking.model.state.TrackingEntityState

abstract class TrackingEntity(val uniqueId : Int) {
    var state = TrackingEntityState.Detected

    var stateChangeTimeStamp = TimeKeeper.millis()
        private set

    // last updated by active region (frame timestamp)
    var detectionUpdatedTimeStamp = 0L

    // methods
    fun updateState(state: TrackingEntityState) {
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
        get() = state == TrackingEntityState.Alive || state == TrackingEntityState.Missing
}