package ch.zhdk.tracking.model.easing

import ch.zhdk.tracking.model.easing.EasingObject
import processing.core.PVector

class EasingVector(var easing: Float = 0.1f) : PVector(), EasingObject {
    var target: PVector = PVector()

    override fun update() {
        add(PVector.sub(target, this).mult(easing))
    }
}