package ch.zhdk.tracking.pipeline.clustering

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.PipelineStep

abstract class MarkerClusterer(val pipeline : Pipeline, config : PipelineConfig = PipelineConfig()) : PipelineStep(config) {
    abstract fun clusterMarkersToDevices(markers: MutableList<Marker>, devices: List<TactileDevice>)
}