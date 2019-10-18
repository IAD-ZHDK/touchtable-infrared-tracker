package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.toTactileObject

class DistanceRegionTracker(pipeline : Pipeline, config : PipelineConfig = PipelineConfig()) : RegionTracker(pipeline, config) {
    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {
        // reset all regions
        objects.forEach { it.isAlive = false }

        // create matrix
        matchNearest(objects, regions, config.maxDelta.value)

        // remove dead regions
        objects.removeAll { !it.isAlive && it.deadTime > config.maxDeadTime.value }

        // update regions
        objects.forEach {
            it.lifeTime++

            // check if object is dead
            if(!it.isAlive) {
                it.deadTime++
            } else {
                // reset dead time
                it.deadTime = 0
            }

            // send object deleted notification
            if(!it.isAlive && it.deadTime > config.maxDeadTime.value && !it.deadNotified) {
                pipeline.onObjectRemoved(it)
                it.deadNotified = true
            }

            // send object detected notification
            if(it.lifeTime > config.minLifeTime.value && !it.aliveNotified) {
                pipeline.onObjectDetected(it)
                it.aliveNotified = true
            }
        }

        // create new regions
        objects.addAll(regions.filter { !it.matched }.map {
            val uniqueId = config.uniqueId.value + 1
            config.uniqueId.setSilent(uniqueId)
            it.toTactileObject(uniqueId)
        })
    }

    private fun matchNearest(objects: MutableList<TactileObject>, regions: List<ActiveRegion>, maxDelta : Double) {
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
                val regionIndex = distances[i].indexOf(minDelta)
                region.toTactileObject(objects[regionIndex])
                objects[regionIndex].isAlive = true

                region.matched = true
            }
        }
    }
}