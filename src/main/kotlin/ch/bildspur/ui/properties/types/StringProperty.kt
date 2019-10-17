package ch.bildspur.ui.properties.types

import ch.bildspur.model.DataModel
import ch.bildspur.ui.EditTextField
import ch.bildspur.ui.properties.StringParameter
import javafx.application.Platform
import javafx.scene.control.TextField
import java.lang.reflect.Field

class StringProperty(field: Field, obj: Any, val annotation: StringParameter) : BaseProperty(field, obj) {

    val textField = EditTextField()

    init {
        textField.prefWidth = 180.0
        textField.isEditable = annotation.isEditable
        applyStyle()

        children.add(textField)

        // setup binding
        val model = field.get(obj) as DataModel<Any>
        model.onChanged += {
            Platform.runLater {
                textField.text = model.value.toString()
            }
        }
        model.fireLatest()

        textField.setOnAction {
            model.value = textField.text
            propertyChanged(this)
            applyStyle()
        }
    }

    private fun applyStyle() {
        if (annotation.isEditable) {
            textField.style = ""
        } else {
            // set to read only
            textField.style = "-fx-background-color: rgba(200, 200, 200, 0.3);\n" +
                    "-fx-border-color: rgba(200, 200, 200, 1.0);\n" +
                    "-fx-border-width: 1px;"
        }
    }
}