package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.pipeline.clustering.DistanceMarkerClusterer
import ch.zhdk.tracking.pipeline.detection.ConventionalRegionDetector
import ch.zhdk.tracking.pipeline.identification.BinaryObjectIdentifier
import ch.zhdk.tracking.pipeline.tracking.DistanceRegionTracker
import org.bytedeco.opencv.opencv_core.Mat


class SimpleTrackingPipeline(config: PipelineConfig, inputProvider: InputProvider, pipelineLock: Any = Any()) :
    Pipeline(config, inputProvider, pipelineLock) {

    private val regionDetector = ConventionalRegionDetector(config)
    private val regionTracker = DistanceRegionTracker(this, config)
    private val markerClusterer = DistanceMarkerClusterer(this, config)
    private val objectIdentifier = BinaryObjectIdentifier(config)

    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        return regionDetector.detectRegions(frame, timestamp)
    }

    override fun mapRegionsToMarkers(markers: MutableList<Marker>, regions: List<ActiveRegion>) {
        regionTracker.mapRegionsToMarkers(markers, regions)
    }

    override fun clusterMarkersToDevices(markers: MutableList<Marker>, devices: List<TactileDevice>) {
        markerClusterer.clusterMarkersToDevices(markers, devices)
    }

    override fun recognizeObjectId(devices: List<TactileDevice>) {
        if (config.identificationEnabled.value)
            objectIdentifier.recognizeObjectId(devices)
    }

    private fun normalizeObjects(devices: MutableList<TactileDevice>) {
        val tl = config.calibration.topLeft.value
        val br = config.calibration.bottomRight.value

        // add normalized values
        devices.forEach {
            // todo: normalized devices
            /*
            it.normalizedPosition = Point2d(
                it.position.x() / config.inputWidth.value,
                it.position.y() / config.inputHeight.value
            )
            it.normalizedIntensity = it.intensity / (config.inputWidth.value * config.inputHeight.value)

            it.calibratedPosition = Point2d(
                it.normalizedPosition.x().map(tl.x.toDouble(), br.x.toDouble(), 0.0, 1.0),
                it.normalizedPosition.y().map(tl.y.toDouble(), br.y.toDouble(), 0.0, 1.0)
            )
             */
        }
    }
}