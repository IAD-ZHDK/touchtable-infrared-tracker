package ch.zhdk.tracking.pipeline.clustering

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.state.TrackingEntityState
import ch.zhdk.tracking.pipeline.Pipeline
import ch.zhdk.tracking.pipeline.toMarker

class DistanceClusterer(pipeline: Pipeline, config: PipelineConfig = PipelineConfig()) :
    MarkerClusterer(pipeline, config) {

    override fun clusterMarkersToDevices(markers: MutableList<Marker>, devices: List<TactileDevice>) {
        // todo: implement
    }
}