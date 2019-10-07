package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.identification.Identification
import ch.zhdk.tracking.model.identification.IntensitySample
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.model.identification.Flank
import ch.zhdk.tracking.model.identification.FlankType
import org.nield.kotlinstatistics.binByDouble
import kotlin.math.roundToLong
import java.util.Arrays.asList
import de.pschoepf.naturalbreaks.JenksFisher



class BinaryObjectIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {

    override fun recognizeObjectId(objects: List<TactileObject>) {
        objects.forEach {
            if(it.lifeTime > config.minLifeTime.value) {
                when (it.identification.identifierPhase) {
                    BinaryIdentifierPhase.Requested -> start(it)
                    BinaryIdentifierPhase.Sampling -> sampling(it)
                    BinaryIdentifierPhase.Identifying -> identify(it)
                    BinaryIdentifierPhase.Detected -> {
                    }
                }
            }
        }
    }

    private fun start(tactileObject: TactileObject) {
        // clear detection
        tactileObject.identification.samples.clear()

        tactileObject.identification.samplingTimer.duration = (config.samplingTime.value * 1000.0).roundToLong()
        tactileObject.identification.samplingTimer.reset()

        println("SamplingTime: ${tactileObject.identification.samplingTimer.duration}")

        tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Sampling
    }

    private fun sampling(tactileObject: TactileObject) {
        tactileObject.identification.samples.add(
            IntensitySample(
                tactileObject.intensity,
                tactileObject.timestamp
            )
        )

        // todo: use frame timer for sample time detection (makes more sense)
        if (tactileObject.identification.samplingTimer.elapsed()) {
            // if sampling time is over
            println("Sampled Intensities: ${tactileObject.identification.samples.count()}")
            tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Identifying
        }
    }

    private fun identify(tactileObject: TactileObject) {
        if (!detectNaturalThreshold(tactileObject.identification)) {
            // something went wrong -> restart process
            tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Requested
            return
        }

        // detect flanks
        val flanks = detectFlanks(tactileObject.identification)
        println("Found ${flanks.size} Flanks!")
        println("Pattern : ${flanks.joinToString { it.type.toString().first().toString() }}")

        // check if two stop bits are detected
        if (flanks.filter { it.type == FlankType.Stop }.size < 2) {
            println("too few stop bits find for detection")
            tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Requested
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
            tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Requested
            return
        }

        // convert to int (MSB - LSB (little-endian))
        var id = 0
        interpolatedFlanks.takeLast(8).reversed().forEachIndexed { index, flank ->
            if(flank.type == FlankType.High)
                id = 1 shl index or id
        }
        println("Id: $id")
        tactileObject.identifier = id

        // cleanup
        tactileObject.identification.samples.clear()

        // mark as detected
        tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Detected
    }

    private fun detectNaturalThreshold(identification: Identification): Boolean {
        val intensities = identification.samples.map { it.intensity }
        val breaks = JenksFisher.createJenksFisherBreaksArray(intensities, 3)
        println("Breaks: ${breaks.joinToString { it.toString() }}")

        // create thresholds and margin
        val minDistance = breaks.zipWithNext { a, b -> b - a }.min()!!
        val thresholdMargin = minDistance / 2.0 * config.thresholdMarginFactor.value

        identification.thresholdMargin = thresholdMargin
        identification.lowThreshold = breaks[0]
        identification.highThreshold = breaks[1]
        identification.stopBitThreshold = breaks[2]

        // print infos
        println("Threshold Margin: ${identification.thresholdMargin}")
        println("Stop Bit: ${identification.stopBitThreshold}")
        println("High Bit: ${identification.highThreshold}")
        println("Low Bit: ${identification.lowThreshold}")

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
                flanks.add(flank)
            }

            last = flank
        }

        return flanks
    }

    private fun interpolateFlanks(flanks: List<Flank>): List<Flank> {
        val result = mutableListOf<Flank>()

        // get longest gap between stop bits (todo: is that really the best heuristic?)
        val stopFlanksIndices = flanks.mapIndexed { index, flank -> Pair(index, flank) }
            .filter { it.second.type == FlankType.Stop }.map { it.first }
        val flankIndex = stopFlanksIndices.zipWithNext { a, b -> b - a }
            .mapIndexed { index, value -> Pair(index, value) }
            .maxBy { it.second }!!.first
        val flankPattern = flanks.subList(stopFlanksIndices[flankIndex], stopFlanksIndices[flankIndex + 1] + 1)

        println("Longest: ${flankPattern.joinToString { it.type.toString().first().toString() }}")

        // todo: better gap detection (min gap is not valid => remove magic numbers
        // detect gaps length (drop first stop bit)
        val gaps = flankPattern.zipWithNext { a, b -> b.timestamp - a.timestamp }
        val minGap = gaps.min()!!
        val marginGap = (minGap * 1.0).roundToLong() // todo: check this 1.25 magic number

        println("MinGap: $minGap")
        println("MarginGap: $marginGap")
        println("Gaps: ${gaps.joinToString { it.toString()}}")

        for (i in 1 until flankPattern.size - 1) {
            val flank = flankPattern[i]

            result.add(flank)

            // check for gap timing
            var gap = gaps[i - 1]

            if(gap < marginGap)
                continue

            // alternative gap interpolation with adding a extra bit if rounded
            val steps = (gap.toDouble() / marginGap.toDouble()).roundToLong()
            (0 until steps - 1).forEach {
                gap -= marginGap
                result.add(Flank(flank.type, flank.timestamp + gap))
            }

            /*
            // old gap interpolation method
            while (gap > marginGap) {
                gap -= marginGap
                result.add(Flank(flank.type, flank.timestamp + gap))
            }
             */
        }

        return result
    }
}