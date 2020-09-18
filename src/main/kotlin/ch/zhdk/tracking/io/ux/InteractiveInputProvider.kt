package ch.zhdk.tracking.io.ux

import ch.bildspur.math.Float2
import ch.zhdk.tracking.io.InputProvider
import ch.zhdk.tracking.javacv.*
import javafx.scene.canvas.Canvas
import javafx.scene.input.KeyCode
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_UNCHANGED
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Rect
import java.nio.file.Path
import kotlin.math.roundToInt

class InteractiveInputProvider(val canvas: Canvas, val markerPath: Path) : InputProvider(1280, 720) {
    lateinit var markerImage: Mat
    var position = Float2(width / 2f, height / 2f)
    var target = Float2(position)
    var rotation = 10.0

    init {
        canvas.setOnScroll {
            rotation += it.deltaY
        }

        canvas.setOnMousePressed {
            target.x = it.x.toFloat()
            target.y = it.y.toFloat()
        }
    }

    override fun open() {
        markerImage = opencv_imgcodecs.imread(markerPath.toAbsolutePath().toString())
        super.open()
    }

    override fun read(): Frame {
        val frame = Mat.zeros(height, width, opencv_core.CV_8UC3).asMat()

        // limit speed
        Thread.sleep(10)

        // update position
        position += (target - position) * 0.05f

        // add marker
        val roi = frame.checkedROI(
            Rect(
                (position.x - markerImage.width() * 0.5).roundToInt(),
                (position.y - markerImage.height() * 0.5).roundToInt(),
                markerImage.width(), markerImage.height()
            )
        )

        // rotate
        val rotatedMarker = markerImage.warpAffineCenter(rotation, 1.0)
        rotatedMarker.copyTo(roi)

        return frame.toFrame()
    }

    override fun close() {
        super.close()
    }
}