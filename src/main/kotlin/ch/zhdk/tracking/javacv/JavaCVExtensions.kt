package ch.zhdk.tracking.javacv

import org.bytedeco.javacpp.Pointer
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.opencv_core.Mat
import org.opencv.imgproc.Imgproc
import processing.core.PImage
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.nio.ByteBuffer
import java.nio.ByteOrder

private var converterToMat = OpenCVFrameConverter.ToMat()

fun Frame.toMat() : Mat {
    return converterToMat.convert(this)
}

fun Mat.toOpenCVMat() : org.opencv.core.Mat {
    return org.opencv.core.Mat(this.address())
}

fun org.opencv.core.Mat.toJavaCVMat() : Mat {
    return Mat(this.nativeObjAddr)
}

fun PImage.toMat(m: org.opencv.core.Mat) {
    val matPixels = ((this.native as BufferedImage).raster.dataBuffer as DataBufferInt).data

    val bb = ByteBuffer.allocate(matPixels.size * 4)
    val ib = bb.asIntBuffer()
    ib.put(matPixels)

    val bvals = bb.array()

    m.put(0, 0, bvals)
}

fun org.opencv.core.Mat.toPImage(img: PImage) {
    img.loadPixels()

    when {
        this.channels() == 3 -> {
            val m2 = org.opencv.core.Mat()
            Imgproc.cvtColor(this, m2, Imgproc.COLOR_RGB2RGBA)
            img.pixels = m2.toARGBPixels()
        }
        this.channels() == 1 -> {
            val m2 = org.opencv.core.Mat()
            Imgproc.cvtColor(this, m2, Imgproc.COLOR_GRAY2RGBA)
            img.pixels = m2.toARGBPixels()
        }
        this.channels() == 4 -> img.pixels = this.toARGBPixels()
    }

    img.updatePixels()
}

fun org.opencv.core.Mat.toARGBPixels(): IntArray {
    val pImageChannels = 4
    val numPixels = this.width() * this.height()
    val intPixels = IntArray(numPixels)
    val matPixels = ByteArray(numPixels * pImageChannels)

    this.get(0, 0, matPixels)
    ByteBuffer.wrap(matPixels).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get(intPixels)
    return intPixels
}