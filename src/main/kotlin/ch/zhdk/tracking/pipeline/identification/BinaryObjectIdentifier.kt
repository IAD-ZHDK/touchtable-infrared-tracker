package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.identification.Identification
import ch.zhdk.tracking.model.identification.IntensitySample
import ch.zhdk.tracking.model.Marker
import ch.zhdk.tracking.model.state.MarkerState
import ch.zhdk.tracking.model.identification.Flank
import ch.zhdk.tracking.model.identification.FlankType
import kotlin.math.roundToLong
import de.pschoepf.naturalbreaks.JenksFisher
import kotlin.math.max


class BinaryObjectIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {

    override fun recognizeObjectId(objects: List<Marker>) {
        objects.filter { it.state == MarkerState.Alive
                && it.timeSinceLastStateChange > config.minDetectedTime.value }
            .forEach {
            when (it.identification.identifierPhase) {
                BinaryIdentifierPhase.Requested -> start(it)
                BinaryIdentifierPhase.Sampling -> sampling(it)
                BinaryIdentifierPhase.Identifying -> identify(it)
                BinaryIdentifierPhase.Detected -> {
                }
            }
        }
    }

    private fun start(marker: Marker) {
        // clear detection and add start timestamp
        marker.identification.samples.clear()
        marker.identification.startFrame = marker.detectionUpdatedTimeStamp

        marker.identification.identifierPhase = BinaryIdentifierPhase.Sampling

        println("----")
    }

    private fun sampling(marker: Marker) {
        marker.identification.samples.add(
            IntensitySample(
                marker.intensity,
                marker.detectionUpdatedTimeStamp
            )
        )

        if (marker.detectionUpdatedTimeStamp - marker.identification.startFrame >= config.sampleCount.value) {
            // if sampling time is over
            marker.identification.identifierPhase = BinaryIdentifierPhase.Identifying
        }
    }

    private fun identify(marker: Marker) {
        if (!detectNaturalThreshold(marker.identification)) {
            // something went wrong -> restart process
            marker.identification.identifierPhase = BinaryIdentifierPhase.Requested
            return
        }

        // detect flanks
        val flanks = detectFlanks(marker.identification)
        println("Found ${flanks.size} Flanks!")
        println("Pattern : ${flanks.joinToString { it.type.toString().first().toString() }}")

        // check if two stop bits are detected
        if (flanks.filter { it.type == FlankType.Stop }.size < 2) {
            println("=> too few stop bits found for detection")
            marker.identification.identifierPhase = BinaryIdentifierPhase.Requested
            return
        }

        // detect binary pattern
        val interpolatedFlanks = interpolateFlanks(flanks)
        println("Interpolated (${interpolatedFlanks.size}) : ${interpolatedFlanks.joinToString {
            it.type.toString().first().toString()
        }}")

        // if to few indices were detected
        if (interpolatedFlanks.size < 7) {
            println("too few bits read")
            marker.identification.identifierPhase = BinaryIdentifierPhase.Requested
            return
        }

        // convert to int (MSB - LSB (little-endian))
        var id = 0
        interpolatedFlanks.takeLast(8).reversed().forEachIndexed { index, flank ->
            if (flank.type == FlankType.High)
                id = id or (1 shl index)
        }
        println("Id: $id")
        marker.identifier = id

        // cleanup
        marker.identification.samples.clear()

        // mark as detected
        marker.identification.identifierPhase = BinaryIdentifierPhase.Requested // todo: change back to detected
    }

    private fun detectNaturalThreshold(identification: Identification): Boolean {
        val intensities = identification.samples.map { it.intensity }

        val breaks = try {
            JenksFisher.createJenksFisherBreaksArray(intensities, 3)
        } catch (ex: Exception) {
            println("JenksFisher error: ${ex.message}")
            return false
        }
        println("Natural Breaks: ${breaks.joinToString { it.toString() }}")

        // create thresholds and margin
        val minDistance = breaks.zipWithNext { a, b -> b - a }.min()!!
        val thresholdMargin = minDistance / 2.0 * config.thresholdMarginFactor.value

        identification.thresholdMargin = thresholdMargin
        identification.lowThreshold = breaks[0]
        identification.highThreshold = breaks[1]
        identification.stopBitThreshold = breaks[2]

        // print infos
        println("Threshold Margin: ${identification.thresholdMargin}")

        return true
    }

    private fun detectFlanks(identification: Identification): List<Flank> {
        val flanks = mutableListOf<Flank>()

        // detect flanks
        var last = identification.getFlank(identification.samples[0])
        flanks.add(last)

        for (i in 1 until identification.samples.size) {
            val flank = identification.getFlank(identification.samples[i])

            // check if change
            if (flank.type != last.type) {
                // todo: maybe adjust timestamp to between flanks
                flank.timestamp -= ((flank.timestamp - last.timestamp) / 2.0).roundToLong()
                flanks.add(flank)
            }

            last = flank
        }

        return flanks
    }

    private fun interpolateFlanks(flanks: List<Flank>): List<Flank> {
        val result = mutableListOf<Flank>()

        // get longest gap between stop bits (todo: is that really the best heuristic?)
        // todo: better => use most accurate timing (by standard)
        val stopFlanksIndices = flanks.mapIndexed { index, flank -> Pair(index, flank) }
            .filter { it.second.type == FlankType.Stop }.map { it.first }
        val flankIndex = stopFlanksIndices.zipWithNext { a, b -> b - a }
            .mapIndexed { index, value -> Pair(index, value) }
            .maxBy { it.second }!!.first
        val flankPattern = flanks.subList(stopFlanksIndices[flankIndex], stopFlanksIndices[flankIndex + 1] + 1)

        println("Longest: ${flankPattern.joinToString { it.type.toString().first().toString() }}")

        // todo: better gap detection (min gap is not valid => remove magic numbers
        // detect gaps length from s to s
        val gaps = flankPattern.zipWithNext { a, b -> b.timestamp - a.timestamp }
        val minGap = max(gaps.min()!!, 2L) // (gaps.sum() / 8.0).roundToLong()

        println("Gaps: ${gaps.joinToString { it.toString() }}")
        println("Min Gap: $minGap")

        for (i in 1 until flankPattern.size - 1) {
            val flank = flankPattern[i]

            result.add(flank)

            // check for gap timing
            var gap = gaps[i]

            if (gap <= minGap)
                continue

            // alternative gap interpolation with adding a extra bit if rounded
            val steps = (gap.toDouble() / minGap.toDouble()).roundToLong()
            (0 until steps - 1).forEach {
                gap -= minGap
                result.add(Flank(flank.type, flank.timestamp + gap))
            }
        }

        return result
    }
}