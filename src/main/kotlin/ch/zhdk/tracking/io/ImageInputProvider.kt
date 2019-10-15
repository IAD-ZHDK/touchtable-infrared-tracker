package ch.zhdk.tracking.io

import ch.zhdk.tracking.javacv.toFrame
import org.bytedeco.javacv.Frame
import org.bytedeco.opencv.global.opencv_imgcodecs.imread
import java.nio.file.Path

class ImageInputProvider(val imageFilePath : Path) : InputProvider() {
    lateinit var image : Frame

    override fun open() {
        val src = imread(imageFilePath.toAbsolutePath().toString())
        image = src.toFrame()

        super.open()
    }

    override fun read(): Frame {
        return image.clone()
    }

    override fun close() {
        super.close()
    }

}