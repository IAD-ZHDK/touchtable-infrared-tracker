package ch.zhdk.tracking.model.easing

class EasingFloat(var easing: Float = 0.1f) : EasingObject {
    var value: Float = 0f
    var target: Float = 0f

    override fun update() {
        value += (target - value) * easing
    }
}