package ch.zhdk.tracking.ui.properties.types

import ch.zhdk.tracking.model.DataModel
import ch.zhdk.tracking.ui.properties.StringParameter
import javafx.scene.control.TextField
import java.lang.reflect.Field

class StringProperty(field: Field, obj: Any, val annotation: StringParameter) : BaseProperty(field, obj) {

    val textField = TextField()

    init {
        textField.prefWidth = 180.0
        textField.isEditable = annotation.isEditable
        applyStyle()

        children.add(textField)

        // setup binding
        val model = field.get(obj) as DataModel<String>
        model.onChanged += {
            textField.text = model.value
        }
        model.fireLatest()

        textField.setOnKeyPressed {
            // change in field
            applyStyle(true)
        }

        textField.setOnAction {
            model.value = textField.text
            propertyChanged(this)
            applyStyle()
        }
    }

    private fun applyStyle(warning : Boolean = false) {
        if (annotation.isEditable) {
            textField.style = ""
        } else {
            // set to read only
            textField.style = "-fx-background-color: rgba(200, 200, 200, 0.3);\n" +
                    "-fx-border-color: rgba(200, 200, 200, 1.0);\n" +
                    "-fx-border-width: 1px;"
        }

        if(warning)
        textField.style += "-fx-text-box-border: #ff9f43; -fx-focus-color: #ff9f43;"
    }
}