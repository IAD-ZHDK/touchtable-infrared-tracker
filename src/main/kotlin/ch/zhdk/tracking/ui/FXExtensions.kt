package ch.zhdk.tracking.ui

import javafx.scene.canvas.GraphicsContext

fun GraphicsContext.strokeCross(x : Double, y : Double, diameter : Double) {
    val hd = diameter * 0.5
    this.strokeLine(x, y - hd, x, y + hd)
    this.strokeLine(x - hd, y, x + hd, y)
}

fun GraphicsContext.strokeX(x : Double, y : Double, diameter : Double) {
    val hd = diameter * 0.5
    this.strokeLine(x - hd, y - hd, x + hd, y + hd)
    this.strokeLine(x - hd, y + hd, x + hd, y - hd)
}

fun GraphicsContext.strokeCircle(x : Double, y : Double, diameter : Double) {
    val hd = diameter * 0.5
    this.strokeOval(x - hd, y - hd, diameter, diameter)
}