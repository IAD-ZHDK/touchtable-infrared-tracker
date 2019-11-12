package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.state.MarkerState
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.toMarker

class DistanceRegionTracker(pipeline: Pipeline, config: PipelineConfig = PipelineConfig()) :
    RegionTracker(pipeline, config) {
    override fun mapRegionToObjects(objects: MutableList<Marker>, regions: List<ActiveRegion>) {
        // reset all regions
        objects.forEach { it.matchedWithRegion = false }

        // create matrix
        matchNearest(objects, regions, config.maxDelta.value)

        // set object state
        objects.forEach {
            val lastSwitchTime = it.timeSinceLastStateChange

            when(it.state) {
                MarkerState.Detected -> {
                    // was detected for long enough
                    if(it.matchedWithRegion && lastSwitchTime > config.minDetectedTime.value) {
                        it.updateState(MarkerState.Alive)
                        pipeline.onObjectDetected(it)
                    }

                    // if missing in start directly dead
                    if(!it.matchedWithRegion) {
                        it.updateState(MarkerState.Dead)
                    }
                }

                MarkerState.Alive -> {
                    // switch to missing if not matched
                    if(!it.matchedWithRegion)
                        it.updateState(MarkerState.Missing)
                }

                MarkerState.Missing -> {
                    // switch back to alive
                    if(it.matchedWithRegion) {
                        it.updateState(MarkerState.Alive)
                    }

                    // switch to dead if time is up
                    if(lastSwitchTime > config.maxMissingTime.value) {
                        it.updateState(MarkerState.Dead)
                        pipeline.onObjectRemoved(it)
                    }
                }
            }
        }

        // remove dead objects
        objects.removeAll { it.state == MarkerState.Dead }

        // create new regions
        objects.addAll(regions.filter { !it.matched }.map {
            val uniqueId = config.uniqueId.value + 1
            config.uniqueId.setSilent(uniqueId)
            it.toMarker(uniqueId)
        })
    }

    private fun matchNearest(objects: MutableList<Marker>, regions: List<ActiveRegion>, maxDelta: Double) {
        // create matrix (point to region)
        val distances = Array(regions.size) { DoubleArray(objects.size) }

        // fill matrix O((m*n)^2)
        regions.forEachIndexed { i, region ->
            objects.forEachIndexed { j, obj ->
                distances[i][j] = obj.position.distance(region.center)
            }
        }

        // match best region to point
        regions.forEachIndexed { i, region ->
            val minDelta = distances[i].min() ?: Double.MAX_VALUE

            if (minDelta <= maxDelta) {
                // existing object found
                val regionIndex = distances[i].indexOf(minDelta)
                region.toMarker(objects[regionIndex])
                objects[regionIndex].matchedWithRegion = true

                region.matched = true
            }
        }
    }
}