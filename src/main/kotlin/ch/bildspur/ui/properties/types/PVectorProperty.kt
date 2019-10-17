package ch.bildspur.ui.properties.types

import ch.bildspur.model.DataModel
import ch.bildspur.ui.NumberField
import ch.bildspur.ui.properties.PVectorParameter
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.control.TextFormatter
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.converter.FloatStringConverter
import processing.core.PVector
import java.lang.reflect.Field

class PVectorProperty(field: Field, obj: Any, val annotation: PVectorParameter) : BaseProperty(field, obj) {

    val model = field.get(obj) as DataModel<PVector>
    val xField = NumberField<Float>(TextFormatter(FloatStringConverter()))
    val yField = NumberField<Float>(TextFormatter(FloatStringConverter()))
    val zField = NumberField<Float>(TextFormatter(FloatStringConverter()))

    val fields = mapOf(
            Pair("X", xField),
            Pair("Y", yField),
            Pair("Z", zField))

    init {
        val box = VBox()
        box.spacing = 5.0

        // setup fields
        fields.forEach {
            val label = Label("${it.key}:")

            it.value.prefWidth = 160.0
            label.prefWidth = 20.0

            it.value.setOnAction {
                model.value = PVector(
                        xField.value.toFloat(),
                        yField.value.toFloat(),
                        zField.value.toFloat())
            }

            box.children.add(HBox(label, it.value))
        }

        // setup binding
        model.onChanged += {
            Platform.runLater {
                xField.value = model.value.x.toDouble()
                yField.value = model.value.y.toDouble()
                zField.value = model.value.z.toDouble()
            }
        }
        model.fireLatest()
        children.add(box)
    }
}