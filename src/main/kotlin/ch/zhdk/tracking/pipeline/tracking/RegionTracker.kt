package ch.zhdk.tracking.pipeline.tracking

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.PipelineStep

abstract class RegionTracker(val pipeline : Pipeline, config : PipelineConfig = PipelineConfig()) : PipelineStep(config) {
    abstract fun mapRegionToObjects(objects: MutableList<Marker>, regions: List<ActiveRegion>)
}