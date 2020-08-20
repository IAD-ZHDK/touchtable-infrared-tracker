package ch.zhdk.tracking.pipeline

import ch.zhdk.tracking.config.PipelineConfig

abstract class PipelineStep(var config : PipelineConfig = PipelineConfig()) {
    open fun pipelineStartup() {}
    open fun pipelineStop() {}
}