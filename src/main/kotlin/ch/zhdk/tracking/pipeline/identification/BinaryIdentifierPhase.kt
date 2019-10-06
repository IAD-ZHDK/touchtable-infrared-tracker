package ch.zhdk.tracking.pipeline.identification

enum class BinaryIdentifierPhase {
    Sampling,
    ThresholdFinding,
    FlankDetection,
    BitPatternRecognition,
    Detected
}