package ch.bildspur.ui.properties.types

import ch.bildspur.model.DataModel
import ch.bildspur.ui.properties.TextParameter
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.layout.VBox
import java.lang.reflect.Field

class TextProperty(field: Field, obj: Any, val annotation: TextParameter) : BaseProperty(field, obj) {

    val textArea = TextArea()
    val submitButton = Button("Submit")
    val box = VBox(textArea)

    init {
        textArea.isEditable = annotation.isEditable

        if (!annotation.isEditable) {
            // set to read only
            textArea.style = "-fx-background-color: rgba(200, 200, 200, 0.3);\n" +
                    "-fx-border-color: rgba(200, 200, 200, 1.0);\n" +
                    "-fx-border-width: 1px;"
        } else {
            box.children.add(submitButton)
        }

        textArea.prefWidth = annotation.width
        textArea.prefHeight = annotation.height
        textArea.isWrapText = annotation.wordWrap
        box.spacing = 5.0
        children.add(box)

        // setup binding
        val model = field.get(obj) as DataModel<String>
        model.onChanged += {
            textArea.text = model.value
        }
        model.fireLatest()

        submitButton.setOnAction {
            model.value = textArea.text
            propertyChanged(this)
        }
    }
}