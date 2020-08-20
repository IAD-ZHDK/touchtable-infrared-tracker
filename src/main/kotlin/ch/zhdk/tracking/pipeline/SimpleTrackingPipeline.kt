package ch.zhdk.tracking.pipeline

import ch.bildspur.util.map
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.angleOfInDeg
import ch.zhdk.tracking.math.linearNormalize
import ch.zhdk.tracking.math.normalize
import ch.zhdk.tracking.math.perspectiveTransform
import ch.zhdk.tracking.model.ActiveRegion
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.pipeline.clustering.DistanceMarkerClusterer
import ch.zhdk.tracking.pipeline.detection.ConventionalRegionDetector
import ch.zhdk.tracking.pipeline.identification.BLEIdentifier
import ch.zhdk.tracking.pipeline.identification.BinaryObjectIdentifier
import ch.zhdk.tracking.pipeline.tracking.DistanceRegionTracker
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point2d
import java.lang.Exception


class SimpleTrackingPipeline(config: PipelineConfig, inputProvider: InputProvider, pipelineLock: Any = Any()) :
    Pipeline(config, inputProvider, pipelineLock) {

    private val regionDetector = ConventionalRegionDetector(config)
    private val regionTracker = DistanceRegionTracker(this, config)
    private val markerClusterer = DistanceMarkerClusterer(this, config)
    private val objectIdentifier = BLEIdentifier(config)

    init {
        steps.addAll(listOf(regionDetector, regionTracker, markerClusterer, objectIdentifier))
    }

    override fun detectRegions(frame: Mat, timestamp: Long): List<ActiveRegion> {
        return regionDetector.detectRegions(frame, timestamp)
    }

    override fun mapRegionsToMarkers(markers: MutableList<Marker>, regions: List<ActiveRegion>) {
        regionTracker.mapRegionsToMarkers(markers, regions)
    }

    override fun clusterMarkersToDevices(markers: MutableList<Marker>, devices: MutableList<TactileDevice>) {
        markerClusterer.clusterMarkersToDevices(markers, devices)
        updateDevices(devices)
    }

    override fun recognizeObjectId(devices: List<TactileDevice>) {
        if (config.identificationEnabled.value)
            objectIdentifier.recognizeObjectId(devices)
    }

    private fun updateDevices(devices: MutableList<TactileDevice>) {
        val width = config.inputWidth.value.toFloat()
        val height = config.inputHeight.value.toFloat()

        val tl = config.calibration.topLeft.value
        val br = config.calibration.bottomRight.value

        devices.forEach {
            // update devices
            it.update()

            // detect basic rotation (2 way)
            if(config.detectSimpleOrientation.value && it.markers.size == 2) {
                it.rotation = it.markers.first().position.angleOfInDeg(it.markers.last().position)
            }

            // add normalized values
            it.normalizedIntensity = it.intensity / (config.inputWidth.value * config.inputHeight.value)

            // calibrate position
            val normalized = it.position.linearNormalize(width.toDouble(), height.toDouble())
            if(config.calibration.perspectiveTransform.value) {
                it.calibratedPosition = normalized.perspectiveTransform(config.calibration.calibrationMat.value)
            } else {
                it.calibratedPosition = Point2d(
                    normalized.x().map(tl.x.toDouble(), br.x.toDouble(), 0.0, 1.0),
                    normalized.y().map(tl.y.toDouble(), br.y.toDouble(), 0.0, 1.0)
                )
            }
        }
    }
}