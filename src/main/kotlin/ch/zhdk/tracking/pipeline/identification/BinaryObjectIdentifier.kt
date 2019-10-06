package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.Identification
import ch.zhdk.tracking.model.TactileObject
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
        tactileObject.identification.intensities.clear()

        tactileObject.identification.samplingTimer.duration = config.samplingTime.value
        tactileObject.identification.samplingTimer.reset()

        tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Sampling
    }

    private fun sampling(tactileObject: TactileObject) {
        tactileObject.identification.intensities.add(tactileObject.intensity)

        // todo: use frame timer for sample time detection (makes more sense)
        if(tactileObject.identification.samplingTimer.elapsed()) {
            // if sampling time is over
            println("Sampled Intensities: ${tactileObject.identification.intensities.count()}")
            tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Identifying
        }
    }

    private fun identify(tactileObject: TactileObject) {
        if(!detectThresholds(tactileObject.identification)) {
            // something went wrong
            return
        }

        // cleanup
        tactileObject.identification.intensities.clear()

        // mark as detected
        tactileObject.identification.identifierPhase = BinaryIdentifierPhase.Detected
    }

    private fun detectThresholds(identification: Identification) : Boolean {
        // find adaptive bin size (double the size of wanted points)
        val valueRange = (identification.intensities.max() ?: 0.0) - (identification.intensities.min() ?: 0.0)
        val adaptiveBinSize = valueRange / 6.0 // todo: maybe resolve magic number (3 bit * 2)

        // find 3 top bins
        val bins = identification.intensities.binByDouble(
            valueSelector = { it },
            binSize = adaptiveBinSize,
            rangeStart = identification.intensities.min()!!
        )
            .filter { it.value.isNotEmpty() }
            .sortedByDescending { it.value.size }
            .take(3)

        // check if it is three bins
        if (bins.size != 3) {
            println("too few bins detected -> go back to sampling")
            identification.identifierPhase = BinaryIdentifierPhase.Requested
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