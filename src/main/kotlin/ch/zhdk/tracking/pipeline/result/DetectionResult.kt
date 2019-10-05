package ch.zhdk.tracking.pipeline.result

import ch.zhdk.tracking.model.ActiveRegion

data class DetectionResult(val regions : List<ActiveRegion>)