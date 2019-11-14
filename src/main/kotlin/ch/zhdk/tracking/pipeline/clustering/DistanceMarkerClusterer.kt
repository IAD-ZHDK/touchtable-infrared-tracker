package ch.zhdk.tracking.pipeline.clustering

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.pipeline.Pipeline
import org.nield.kotlinstatistics.dbScanCluster

class DistanceMarkerClusterer(pipeline: Pipeline, config: PipelineConfig = PipelineConfig()) :
    MarkerClusterer(pipeline, config) {

    override fun clusterMarkersToDevices(markers: MutableList<Marker>, devices: List<TactileDevice>) {
        // get clusters
        val centroids = markers.dbScanCluster(config.maximumRadius.value, 2,
            xSelector = { it.position.x() },
            ySelector = { it.position.y() })

        // map centroids to devices

    }
}