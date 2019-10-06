package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.TactileObject
import org.nield.kotlinstatistics.binByDouble

class BinaryObjectIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {

    override fun recognizeObjectId(objects: List<TactileObject>) {
        objects.forEach {
            when (it.identifierPhase) {
                BinaryIdentifierPhase.Requested -> start(it)
                BinaryIdentifierPhase.Sampling -> sampling(it)
                BinaryIdentifierPhase.Identifying -> identify(it)
                BinaryIdentifierPhase.Detected -> {}
            }
        }
    }

    private fun start(tactileObject: TactileObject) {
        // clear detection
        tactileObject.intensities.clear()

        tactileObject.samplingTimer.duration = config.samplingTime.value
        tactileObject.samplingTimer.reset()

        tactileObject.identifierPhase = BinaryIdentifierPhase.Sampling
    }

    private fun sampling(tactileObject: TactileObject) {
        tactileObject.intensities.add(tactileObject.currentIntensity)

        // todo: use frame timer for sample time detection (makes more sense)
        if(tactileObject.samplingTimer.elapsed()) {
            // if sampling time is over
            println("Sampled Intensities: ${tactileObject.intensities.count()}")
            tactileObject.identifierPhase = BinaryIdentifierPhase.Identifying
        }
    }

    private fun identify(tactileObject: TactileObject) {
        if(!detectThresholds(tactileObject)) {
            // something went wrong
            return
        }

        // cleanup
        tactileObject.intensities.clear()

        // mark as detected
        tactileObject.identifierPhase = BinaryIdentifierPhase.Detected
    }

    private fun detectThresholds(tactileObject: TactileObject) : Boolean {
        // find adaptive bin size (double the size of wanted points)
        val valueRange = (tactileObject.intensities.max() ?: 0.0) - (tactileObject.intensities.min() ?: 0.0)
        val adaptiveBinSize = valueRange / 6.0 // todo: maybe resolve magic number (3 bit * 2)

        // find 3 top bins
        val bins = tactileObject.intensities.binByDouble(
            valueSelector = { it },
            binSize = adaptiveBinSize,
            rangeStart = tactileObject.intensities.min()!!
        )
            .filter { it.value.isNotEmpty() }
            .sortedByDescending { it.value.size }
            .take(3)

        // check if it is three bins
        if (bins.size != 3) {
            println("too few bins detected -> go back to sampling")
            tactileObject.identifierPhase = BinaryIdentifierPhase.Requested
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

        tactileObject.thresholdMargin = thresholdMargin
        tactileObject.lowThreshold = averages[0]
        tactileObject.highThreshold = averages[1]
        tactileObject.stopBitThreshold = averages[2]

        // print infos
        println("Threshold Margin: ${tactileObject.thresholdMargin}")
        println("Stop Bit: ${tactileObject.stopBitThreshold}")
        println("High Bit: ${tactileObject.highThreshold}")
        println("Low Bit: ${tactileObject.lowThreshold}")

        return true
    }
}