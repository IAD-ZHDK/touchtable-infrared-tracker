package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.PipelineStep

abstract class RegionTracker(config : PipelineConfig = PipelineConfig()) : PipelineStep(config) {
    abstract fun mapRegionToObjects(objects: MutableList<TactileObject>, regions: List<ActiveRegion>)
}