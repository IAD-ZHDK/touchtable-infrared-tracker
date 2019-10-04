package ch.bildspur.ui.properties.types

import ch.bildspur.ui.properties.LabelParameter
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import java.lang.reflect.Field

class LabelProperty (field: Field, obj: Any, val annoation: LabelParameter) : BaseProperty(field, obj) {
    private val label = Label(annoation.name)

    init {
        label.padding = Insets(10.0, 0.0, 0.0, 0.0)
        label.font = Font.font("Helvetica", FontWeight.BOLD, 14.0)
        children.add(label)
    }
}