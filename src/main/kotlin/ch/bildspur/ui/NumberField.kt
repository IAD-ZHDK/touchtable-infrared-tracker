package ch.bildspur.ui

import javafx.beans.binding.Bindings
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.util.StringConverter
import javafx.util.converter.NumberStringConverter
import java.text.NumberFormat
import java.util.*

class NumberField<T>(formatter: TextFormatter<T>,
                     groupingEnabled : Boolean = false) : EditTextField() {

    private val converter: StringConverter<Number>
    private val valueProperty: DoubleProperty = SimpleDoubleProperty()

    init {
        // setup converter
        val format = NumberFormat.getInstance(Locale.ENGLISH)
        format.isGroupingUsed = groupingEnabled
        converter = NumberStringConverter(format)

        // set number formatter
        textFormatter = formatter

        // create binding between value and text
        Bindings.bindBidirectional(textProperty(), valueProperty, converter)

        // set design
        this.alignment = Pos.CENTER_RIGHT
    }

    var value : Double
        get() = valueProperty.value
        set(value) = valueProperty.set(value)
}