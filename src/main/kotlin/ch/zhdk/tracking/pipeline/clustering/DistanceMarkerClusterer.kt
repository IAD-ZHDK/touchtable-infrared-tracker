package ch.zhdk.tracking.pipeline.clustering

import ch.bildspur.math.Float2
import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.javacv.center
import ch.zhdk.tracking.javacv.distance
import ch.zhdk.tracking.javacv.tracking.matchNearest
import ch.zhdk.tracking.math.toFloat2
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.TactileDevice
import ch.zhdk.tracking.model.state.TrackingEntityState
import ch.zhdk.tracking.pipeline.Pipeline
import org.bytedeco.opencv.opencv_core.Point2d
import org.nield.kotlinstatistics.Centroid
import org.nield.kotlinstatistics.dbScanCluster

class DistanceMarkerClusterer(pipeline: Pipeline, config: PipelineConfig = PipelineConfig()) :
    MarkerClusterer(pipeline, config) {

    data class MatchCentroid(val centroid: Centroid<Marker>, var matched: Boolean = false) {
        val center = centroid.points.map { it.position }.center()
    }

    override fun clusterMarkersToDevices(markers: MutableList<Marker>, devices: MutableList<TactileDevice>) {
        // get clusters
        val centroids = markers.dbScanCluster(config.maximumRadius.value, 1,
            xSelector = { it.position.x() },
            ySelector = { it.position.y() })
            .map { MatchCentroid(it) }

        // reset all devices
        devices.forEach {
            it.matchedWithCentroid = false
            it.markers.clear()
        }

        // map centroids to devices
        centroids.matchNearest(devices,
            config.deviceMaxDelta.value,
            distance = { s, d -> d.position.distance(s.center) },
            matched = { it.matchedWithCentroid },
            onMatch = { s, d ->
                d.markers.addAll(s.centroid.points)
                d.matchedWithCentroid = true
                s.matched = true
            })

        // set object state
        // todo: use different timings for min detect and dead
        devices.forEach {
            val lastSwitchTime = it.timeSinceLastStateChange

            when (it.state) {
                TrackingEntityState.Detected -> {
                    // was detected for long enough
                    if (it.matchedWithCentroid && lastSwitchTime > config.minDetectedTime.value) {
                        it.updateState(TrackingEntityState.Alive)

                        // init filter
                        if(config.smoothPosition.value) {
                            it.positionFilter.tPrev = 0f
                            it.positionFilter.xPrev = it.calibratedPosition.toFloat2()
                        }

                        pipeline.onDeviceDetected(it)
                    }

                    // if missing in start directly dead
                    if (!it.matchedWithCentroid) {
                        it.updateState(TrackingEntityState.Dead)
                    }
                }

                TrackingEntityState.Alive -> {
                    // switch to missing if not matched
                    if (!it.matchedWithCentroid)
                        it.updateState(TrackingEntityState.Missing)
                }

                TrackingEntityState.Missing -> {
                    // switch back to alive
                    if (it.matchedWithCentroid) {
                        it.updateState(TrackingEntityState.Alive)
                    }

                    // switch to dead if time is up
                    if (lastSwitchTime > config.maxMissingTime.value) {
                        it.updateState(TrackingEntityState.Dead)
                        pipeline.onDeviceRemoved(it)
                    }
                }
                else -> error("Dead centroid detected")
            }
        }

        // remove dead devices
        devices.removeAll { it.state == TrackingEntityState.Dead }

        // create new devices
        devices.addAll(centroids.filter { !it.matched }.map {
            val uniqueDeviceId = config.uniqueTactileObjectId.value + 1
            config.uniqueTactileObjectId.setSilent(uniqueDeviceId)
            // create new device
            val device = TactileDevice(uniqueDeviceId)
            device.markers.addAll(it.centroid.points)
            device.matchedWithCentroid = true
            device
        })
    }
}