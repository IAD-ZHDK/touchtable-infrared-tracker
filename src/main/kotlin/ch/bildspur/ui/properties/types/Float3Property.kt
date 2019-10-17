package ch.bildspur.ui.properties.types

import ch.bildspur.model.DataModel
import ch.bildspur.model.math.Float3
import ch.bildspur.ui.RelationNumberField
import ch.bildspur.ui.properties.Float3Parameter
import javafx.application.Platform
import javafx.scene.control.Label
import javafx.scene.control.TextFormatter
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.util.converter.FloatStringConverter
import java.lang.reflect.Field

class Float3Property(field: Field, obj: Any, val annotation: Float3Parameter) : BaseProperty(field, obj) {

    val model = field.get(obj) as DataModel<Float3>
    val xField = RelationNumberField<Float>(TextFormatter(FloatStringConverter()))
    val yField = RelationNumberField<Float>(TextFormatter(FloatStringConverter()))
    val zField = RelationNumberField<Float>(TextFormatter(FloatStringConverter()))

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

            it.value.prefWidth = RelationNumberField.PREFERRED_WIDTH - 20.0
            it.value.isShowRange = false
            label.prefWidth = 20.0

            it.value.setOnAction {
                model.value = Float3(
                        xField.getValue().toFloat(),
                        yField.getValue().toFloat(),
                        zField.getValue().toFloat())
            }

            box.children.add(HBox(label, it.value))
        }

        // setup binding
        model.onChanged += {
            Platform.runLater {
                xField.setValue(model.value.x.toDouble())
                yField.setValue(model.value.y.toDouble())
                zField.setValue(model.value.z.toDouble())
            }
        }
        model.fireLatest()
        children.add(box)
    }
}