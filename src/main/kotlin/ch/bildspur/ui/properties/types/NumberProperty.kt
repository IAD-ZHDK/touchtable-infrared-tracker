package ch.bildspur.ui.properties.types

import ch.bildspur.model.DataModel
import ch.bildspur.ui.NumberField
import ch.bildspur.ui.properties.NumberParameter
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextFormatter
import javafx.scene.layout.HBox
import javafx.scene.text.Font
import javafx.util.converter.NumberStringConverter
import java.lang.reflect.Field
import java.text.NumberFormat
import java.util.*

class NumberProperty(field: Field, obj: Any, val annotation: NumberParameter) : BaseProperty(field, obj) {

    val format = NumberFormat.getInstance(Locale.ENGLISH)
    val numberStringConverter = NumberStringConverter(format)
    val textFormatter = TextFormatter(numberStringConverter)

    val numberField = NumberField<Number>(textFormatter)
    val unitField = Label(annotation.unit)

    val box = HBox(numberField, unitField)

    init {
        format.isGroupingUsed = false
        unitField.font = Font("Helvetica", 8.0)

        box.spacing = 10.0
        box.alignment = Pos.CENTER_LEFT
        children.add(box)

        val model = field.get(obj) as DataModel<Number>
        model.onChanged += {
            Platform.runLater {
                numberField.value = model.value.toDouble()
            }
        }
        model.fireLatest()

        numberField.setOnAction {
            if (model.value is Short)
                model.value = numberField.value.toShort()

            if (model.value is Int)
                model.value = numberField.value.toInt()

            if (model.value is Float)
                model.value = numberField.value.toFloat()

            if (model.value is Double)
                model.value = numberField.value
            propertyChanged(this)
        }
    }
}