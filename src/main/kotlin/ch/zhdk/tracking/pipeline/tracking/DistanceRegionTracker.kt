package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.javacv.tracking.matchNearest
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.state.TrackingEntityState
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.toMarker

class DistanceRegionTracker(pipeline: Pipeline, config: PipelineConfig = PipelineConfig()) :
    RegionTracker(pipeline, config) {
    override fun mapRegionsToMarkers(markers: MutableList<Marker>, regions: List<ActiveRegion>) {
        // reset all regions
        markers.forEach { it.matchedWithRegion = false }

        // create matrix
        regions.matchNearest(markers,
            config.markerMaxDelta.value,
            distance = { s, d -> d.position.distance(s.center) },
            matched = { it.matchedWithRegion },
            onMatch = { s, d ->
                s.toMarker(d)
                d.matchedWithRegion = true
                s.matched = true
            })

        // set object state
        markers.forEach {
            val lastSwitchTime = it.timeSinceLastStateChange

            when (it.state) {
                TrackingEntityState.Detected -> {
                    // was detected for long enough
                    if (it.matchedWithRegion && lastSwitchTime > config.minDetectedTime.value) {
                        it.updateState(TrackingEntityState.Alive)
                        // todo: detect tactile device
                        //pipeline.onDeviceDetected(it)
                    }

                    // if missing in start directly dead
                    if (!it.matchedWithRegion) {
                        it.updateState(TrackingEntityState.Dead)
                    }
                }

                TrackingEntityState.Alive -> {
                    // switch to missing if not matched
                    if (!it.matchedWithRegion)
                        it.updateState(TrackingEntityState.Missing)
                }

                TrackingEntityState.Missing -> {
                    // switch back to alive
                    if (it.matchedWithRegion) {
                        it.updateState(TrackingEntityState.Alive)
                    }

                    // switch to dead if time is up
                    if (lastSwitchTime > config.maxMissingTime.value) {
                        it.updateState(TrackingEntityState.Dead)
                        // todo: remove device
                        //pipeline.onDeviceRemoved(it)
                    }
                }
            }
        }

        // remove dead markers
        markers.removeAll { it.state == TrackingEntityState.Dead }

        // create new regions
        markers.addAll(regions.filter { !it.matched }.map {
            val uniqueMarkerId = config.uniqueMarkerId.value + 1
            config.uniqueMarkerId.setSilent(uniqueMarkerId)
            it.toMarker(uniqueMarkerId)
        })
    }
}