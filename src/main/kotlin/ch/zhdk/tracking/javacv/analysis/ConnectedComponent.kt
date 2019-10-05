package ch.zhdk.tracking.javacv.analysis

import ch.zhdk.tracking.javacv.checkedROI
import org.bytedeco.javacpp.indexer.DoubleRawIndexer
import org.bytedeco.javacpp.indexer.IntRawIndexer
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.*


/**
 * Created by cansik on 05.02.17.
 */
class ConnectedComponent {
    val label: Int

    val position: Point
    val size: Size
    val area: Int

    val centroid: Point2d

    constructor(location: Point, size: Size, area: Int, centroid: Point2d) {
        this.label = -1
        this.position = location
        this.size = size
        this.area = area
        this.centroid = centroid
    }

    constructor(label: Int, rectRow: Mat, centroidRow: Mat) {
        this.label = label

        val rectIndexer = rectRow.createIndexer<IntRawIndexer>()
        val centroidIndexer = centroidRow.createIndexer<DoubleRawIndexer>()

        // copy data
        area = rectIndexer[0, CC_STAT_AREA.toLong()]
        position = Point(rectIndexer[0, CC_STAT_LEFT.toLong()], rectIndexer[0, CC_STAT_TOP.toLong()])
        size = Size(rectIndexer[0, CC_STAT_WIDTH.toLong()], rectIndexer[0, CC_STAT_HEIGHT.toLong()])

        // create centroid
        val centroidData = DoubleArray(2)
        centroidIndexer[0, 0, centroidData]
        centroid = Point2d(centroidData[0], centroidData[1])
    }

    fun getROI(img : Mat) : Mat {
        return img.checkedROI(Rect(position.x(), position.y(), size.width(), size.height()))
    }
}