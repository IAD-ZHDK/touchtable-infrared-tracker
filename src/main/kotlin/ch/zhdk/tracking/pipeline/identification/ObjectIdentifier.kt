package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.TactileObject
import ch.zhdk.tracking.pipeline.PipelineStep

abstract class ObjectIdentifier(config : PipelineConfig = PipelineConfig()) : PipelineStep(config) {
    abstract fun recognizeObjectId(objects: List<TactileObject>)
}