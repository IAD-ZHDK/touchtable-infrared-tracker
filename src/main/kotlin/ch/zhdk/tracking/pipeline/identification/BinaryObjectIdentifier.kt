package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.identification.Identification
import ch.zhdk.tracking.model.identification.IntensitySample
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.model.identification.Flank
import org.nield.kotlinstatistics.binByDouble

class BinaryObjectIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {

    override fun recognizeObjectId(objects: List<TactileObject>) {
        objects.forEach {
            when (it.identification.identifierPhase) {
                BinaryIdentifierPhase.Requested -> start(it)
                BinaryIdentifierPhase.Sampling -> sampling(it)
                BinaryIdentifierPhase.Identifying -> identify(it)
                BinaryIdentifierPhase.Detected -> {}
            }
        }
    }

    private fun start(tactileObject: TactileObject) {
        // clear detection
        tactileObject.identification.samples.clear()

        tactileObject.identification.samplingTimer.duration = config.samplingTime.value
        tactileObject.identification.samplingTimer.reset()

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
        if(tactileObject.identification.samplingTimer.elapsed()) {
            // if sampling time is over
            println("Sampled Intensities: ${tactileObject.identification.samples.count()}")
            tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Identifying
        }
    }

    private fun identify(tactileObject: TactileObject) {
        if(!detectThresholds(tactileObject.identification)) {
            // something went wrong -> restart process
            tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Requested
            return
        }

        // detect flanks
        val flanks =detectFlanks(tactileObject.identification)
        println("Found ${flanks.size} Flanks!")
        println("Pattern: ${flanks.joinToString {  it.type.toString().first().toString() }}")

        // detect binary pattern


        // cleanup
        tactileObject.identification.samples.clear()

        // mark as detected
        tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Detected
    }

    private fun detectFlanks(identification: Identification) : List<Flank> {
        val flanks = mutableListOf<Flank>()

        // detect flanks
        var last = identification.getFlank(identification.samples[0])

        for(i in 1 until identification.samples.size) {
            val flank = identification.getFlank(identification.samples[i])

            // check if change
            if(flank.type != last.type) {
                // todo: maybe adjust timestamp to between flanks
                flanks.add(flank)
            }

            last = flank
        }

        return flanks
    }

    private fun detectThresholds(identification: Identification) : Boolean {
        // prepare intensities
        val intensities = identification.samples.map { it.intensity }

        // find adaptive bin size (double the size of wanted points)
        val valueRange = (intensities.max() ?: 0.0) - (intensities.min() ?: 0.0)
        val adaptiveBinSize = valueRange / 6.0 // todo: maybe resolve magic number (3 bit * 2)

        // find 3 top bins
        val bins = intensities.binByDouble(
            valueSelector = { it },
            binSize = adaptiveBinSize,
            rangeStart = intensities.min()!!
        )
            .filter { it.value.isNotEmpty() }
            .sortedByDescending { it.value.size }
            .take(3)

        // check if it is three bins
        if (bins.size != 3) {
            println("too few bins detected -> go back to sampling")
            return false
        }

        // get top three bins with most values
        bins.forEach {
            println("Range: ${it.range} Count: ${it.value.size}")
        }

        // create thresholds and adaptive margin
        val averages = bins.map { it.value.average() }.sorted()
        val minDistance = averages.zipWithNext { a, b -> b - a }.min() ?: 0.0
        val thresholdMargin = minDistance / 2.0 * config.thresholdMarginFactor.value

        identification.thresholdMargin = thresholdMargin
        identification.lowThreshold = averages[0]
        identification.highThreshold = averages[1]
        identification.stopBitThreshold = averages[2]

        // print infos
        println("Threshold Margin: ${identification.thresholdMargin}")
        println("Stop Bit: ${identification.stopBitThreshold}")
        println("High Bit: ${identification.highThreshold}")
        println("Low Bit: ${identification.lowThreshold}")

        return true
    }
}