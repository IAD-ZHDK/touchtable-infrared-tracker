package ch.zhdk.tracking.pipeline.identification

import ch.zhdk.tracking.config.PipelineConfig
import ch.zhdk.tracking.model.TactileObject

class BinaryObjectIdentifier(config : PipelineConfig = PipelineConfig()) : ObjectIdentifier(config) {
    override fun recognizeObjectId(objects: List<TactileObject>) {

    }
}