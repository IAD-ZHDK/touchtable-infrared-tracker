package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.toTactileObject

class DistanceRegionTracker(config : PipelineConfig = PipelineConfig()) : RegionTracker(config) {
    override fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>) {
        // reset all regions
        objects.forEach { it.isAlive = false }

        // create matrix
        matchNearest(objects, regions, config.maxDelta.value)

        // remove dead regions
        objects.removeAll { !it.isAlive }

        // update regions
        objects.forEach { it.lifeTime++ }

        // create new regions
        objects.addAll(regions.filter { !it.matched }.map { it.toTactileObject(config.uniqueId.value++) })
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