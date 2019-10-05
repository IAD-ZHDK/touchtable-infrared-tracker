package ch.zhdk.tracking.javacv.analysis

import org.bytedeco.javacpp.indexer.DoubleIndexer
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Point2d
import org.bytedeco.opencv.opencv_core.Size
import org.bytedeco.opencv.opencv_core.Size2d


/**
 * Created by cansik on 05.02.17.
 */
class ConnectedComponent {
    val label: Int

    val location: Point2d
    val size: Size2d
    val area: Double

    val centroid: Point2d

    constructor(location: Point2d, size: Size2d, area: Double, centroid: Point2d) {
        this.label = -1
        this.location = location
        this.size = size
        this.area = area
        this.centroid = centroid
    }

    constructor(label: Int, rectRow: Mat, centroidRow: Mat) {
        this.label = label

        val rectIndexer = rectRow.createIndexer<DoubleIndexer>()
        val centroidIndexer = centroidRow.createIndexer<DoubleIndexer>()

        // copy data
        area = rectIndexer[0, CC_STAT_AREA.toLong()]
        location = Point2d(rectIndexer[0, CC_STAT_LEFT.toLong()], rectIndexer[0, CC_STAT_TOP.toLong()])
        size = Size2d(rectIndexer[0, CC_STAT_WIDTH.toLong()], rectIndexer[0, CC_STAT_HEIGHT.toLong()])

        // create centroid
        val centroidData = DoubleArray(2)
        centroidIndexer[0, 0, centroidData]
        centroid = Point2d(centroidData[0], centroidData[1])
    }
}