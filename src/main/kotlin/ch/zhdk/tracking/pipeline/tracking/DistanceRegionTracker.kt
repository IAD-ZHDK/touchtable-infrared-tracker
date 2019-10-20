package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.model.TactileObjectState
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.toTactileObject

class DistanceRegionTracker(pipeline: Pipeline, config: PipelineConfig = PipelineConfig()) :
    RegionTracker(pipeline, config) {
    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {
        // reset all regions
        objects.forEach { it.matchedWithRegion = false }

        // create matrix
        matchNearest(objects, regions, config.maxDelta.value)

        // set object state
        objects.forEach {
            val lastSwitchTime = it.timeSinceLastStateChange

            when(it.state) {
                TactileObjectState.Detected -> {
                    // was detected for long enough
                    if(it.matchedWithRegion && lastSwitchTime > config.minDetectedTime.value) {
                        it.updateState(TactileObjectState.Alive)
                        pipeline.onObjectDetected(it)
                    }

                    // if missing in start directly dead
                    if(!it.matchedWithRegion) {
                        it.updateState(TactileObjectState.Dead)
                    }
                }

                TactileObjectState.Alive -> {
                    // switch to missing if not matched
                    if(!it.matchedWithRegion)
                        it.updateState(TactileObjectState.Missing)
                }

                TactileObjectState.Missing -> {
                    // switch back to alive
                    if(it.matchedWithRegion) {
                        it.updateState(TactileObjectState.Alive)
                    }

                    // switch to dead if time is up
                    if(lastSwitchTime > config.maxMissingTime.value) {
                        it.updateState(TactileObjectState.Dead)
                        pipeline.onObjectRemoved(it)
                    }
                }
            }
        }

        // remove dead objects
        objects.removeAll { it.state == TactileObjectState.Dead }

        // create new regions
        objects.addAll(regions.filter { !it.matched }.map {
            val uniqueId = config.uniqueId.value + 1
            config.uniqueId.setSilent(uniqueId)
            it.toTactileObject(uniqueId)
        })
    }

    private fun matchNearest(objects: MutableList<TactileObject>, regions: List<ActiveRegion>, maxDelta: Double) {
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
                region.toTactileObject(objects[regionIndex])
                objects[regionIndex].matchedWithRegion = true

                region.matched = true
            }
        }
    }
}