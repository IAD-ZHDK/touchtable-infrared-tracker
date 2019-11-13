package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.state.TrackingEntityState
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.toMarker

class DistanceRegionTracker(pipeline: Pipeline, config: PipelineConfig = PipelineConfig()) :
    RegionTracker(pipeline, config) {
    override fun mapRegionToObjects(markers: MutableList<Marker>, regions: List<ActiveRegion>) {
        // reset all regions
        markers.forEach { it.matchedWithRegion = false }

        // create matrix
        matchNearest(markers, regions, config.maxDelta.value)

        // set object state
        markers.forEach {
            val lastSwitchTime = it.timeSinceLastStateChange

            when(it.state) {
                TrackingEntityState.Detected -> {
                    // was detected for long enough
                    if(it.matchedWithRegion && lastSwitchTime > config.minDetectedTime.value) {
                        it.updateState(TrackingEntityState.Alive)
                        // todo: detect tactile device
                        //pipeline.onDeviceDetected(it)
                    }

                    // if missing in start directly dead
                    if(!it.matchedWithRegion) {
                        it.updateState(TrackingEntityState.Dead)
                    }
                }

                TrackingEntityState.Alive -> {
                    // switch to missing if not matched
                    if(!it.matchedWithRegion)
                        it.updateState(TrackingEntityState.Missing)
                }

                TrackingEntityState.Missing -> {
                    // switch back to alive
                    if(it.matchedWithRegion) {
                        it.updateState(TrackingEntityState.Alive)
                    }

                    // switch to dead if time is up
                    if(lastSwitchTime > config.maxMissingTime.value) {
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

    private data class Distance(val index : Int, val distance : Double)

    private fun matchNearest(markers: MutableList<Marker>, regions: List<ActiveRegion>, maxDelta: Double) {
        // create matrix (point to region)
        val distances = Array(regions.size) { DoubleArray(markers.size) }

        // fill matrix O((m*n)^2)
        regions.forEachIndexed { i, region ->
            markers.forEachIndexed { j, marker ->
                distances[i][j] = marker.position.distance(region.center)
            }
        }

        // match best region to marker
        regions.forEachIndexed { i, region ->
            val minDelta = distances[i]
                .mapIndexed { index, distance -> Distance(index, distance) }
                .filter { !markers[it.index].matchedWithRegion }
                .minBy { it.distance } ?: Distance(-1, Double.MAX_VALUE)

            if (minDelta.distance <= maxDelta) {
                // existing object found
                val marker = markers[minDelta.index]
                marker.matchedWithRegion = true

                region.toMarker(marker)
                region.matched = true
            }
        }
    }
}