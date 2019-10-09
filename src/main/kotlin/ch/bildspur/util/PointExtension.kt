package ch.bildspur.util

import ch.fhnw.exakt.util.Point
import kotlin.math.roundToInt

/**
 * Created by cansik on 06.01.17.
 */
fun <T : Point> MutableList<T>.sparsePoints(maxDistance: Double): MutableList<MutableList<T>> {
    val sparseCloud = mutableListOf<MutableList<T>>()
    val points = this.toMutableList()

    while (!points.isEmpty()) {
        val firstPoint = points.removeAt(0)
        val group = mutableListOf<T>()
        val nearPoints = mutableListOf(firstPoint)

        while (!nearPoints.isEmpty()) {
            val point = nearPoints.removeAt(0)
            group.add(point)

            for (p in points.toList()) {
                // calculate distance
                val dist = point.distance(p)

                if (dist < maxDistance) {
                    points.remove(p)
                    nearPoints.add(p)
                }
            }
        }

        sparseCloud.add(group)
    }

    return sparseCloud
}

fun <T : Point> MutableList<MutableList<T>>.combinePoints(): MutableList<Point> {
    val sparsePoints = mutableListOf<Point>()
    this.mapTo(sparsePoints) { pointList -> Point(pointList.map { it.x }.average(), pointList.map { it.y }.average()) }
    return sparsePoints
}

fun Point.distance(p2: Point): Double {
    return Math.sqrt(Math.pow(p2.x - this.x, 2.0) + Math.pow(p2.y - this.y, 2.0))
}

fun Point.map(x1: Double, y1: Double, x2: Double, y2: Double): Point {
    return Point(this.x.map(0.0, x1, 0.0, x2), this.y.map(0.0, y1, 0.0, y2))
}

fun Int.map(start1: Int, stop1: Int, start2: Int, stop2: Int): Int {
    return this.toDouble().map(
        start1.toDouble(),
        stop1.toDouble(),
        start2.toDouble(),
        stop2.toDouble()
    ).roundToInt()
}

fun Double.map(start1: Double, stop1: Double, start2: Double, stop2: Double): Double {
    return start2 + (stop2 - start2) * ((this - start1) / (stop1 - start1))
}