package ch.zhdk.tracking.javacv.analysis

import org.bytedeco.opencv.opencv_core.Mat

/**
 * Created by cansik on 05.02.17.
 */
data class ConnectedComponentsResult(val labeled: Mat, val rectComponents: Mat, val centComponents: Mat) {
    fun release() {
        labeled.release()
        rectComponents.release()
        centComponents.release()
    }

    val length: Int
        get() = centComponents.size().height()

    fun getConnectedComponents(): List<ConnectedComponent> {
        return (0 until length).map { ConnectedComponent(it, rectComponents.row(it), centComponents.row(it)) }
    }
}