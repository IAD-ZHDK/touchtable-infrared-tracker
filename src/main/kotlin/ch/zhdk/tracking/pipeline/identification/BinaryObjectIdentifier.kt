package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.TactileObject
import org.nield.kotlinstatistics.binByDouble

class BinaryObjectIdentifier(config: PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {

    override fun recognizeObjectId(objects: List<TactileObject>) {
        objects.forEach {
            when (it.identifierPhase) {
                BinaryIdentifierPhase.Sampling -> sampling(it)
                BinaryIdentifierPhase.ThresholdFinding -> findThresholds(it)
                BinaryIdentifierPhase.FlankDetection -> flankDetection(it)
                BinaryIdentifierPhase.BitPatternRecognition -> TODO()
            }
        }
    }

    private fun sampling(tactileObject: TactileObject) {
        // if start of sampling
        if (tactileObject.intensities.elementCount == 0) {
            tactileObject.identifierTimer.duration = config.minFlankTime.value
        }

        // if is sampling time
        if (tactileObject.identifierTimer.elapsed()) {
            tactileObject.intensities.add(tactileObject.currentIntensity)
        }

        // if sampling is over (buffer full)
        if (tactileObject.intensities.elementCount == tactileObject.intensities.size) {
            tactileObject.identifierPhase = BinaryIdentifierPhase.ThresholdFinding
        }
    }

    private fun findThresholds(tactileObject: TactileObject) {
        // find adaptive bin size (double the size of wanted points)
        val valueRange = (tactileObject.intensities.max() ?: 0.0) - (tactileObject.intensities.min() ?: 0.0)
        val adaptiveBinSize = valueRange / 6.0 // todo: maybe resolve magic number (3 bit * 2)

        // find 3 top bins
        val bins = tactileObject.intensities.binByDouble(
            valueSelector = { it },
            binSize = adaptiveBinSize,
            rangeStart = tactileObject.intensities.min()!!
        )
            .sortedByDescending { it.value.size }
            .take(3)

        // check if it is three bins
        if (bins.size != 3) {
            println("too few bins detected -> go back to sampling")
            tactileObject.identifierPhase = BinaryIdentifierPhase.Sampling
            return
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

        // cleanup
        tactileObject.intensities.clear()

        // send to next phase
        tactileObject.identifierPhase = BinaryIdentifierPhase.FlankDetection
    }

    private fun flankDetection(tactileObject: TactileObject) {
        // do nothing
    }
}