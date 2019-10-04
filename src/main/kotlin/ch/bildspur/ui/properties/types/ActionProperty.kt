package ch.bildspur.ui.properties.types

import ch.bildspur.ui.properties.ActionParameter
import javafx.application.Platform
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import java.lang.Exception
import java.lang.reflect.Field
import kotlin.concurrent.thread

class ActionProperty(field: Field, obj: Any, val annotation: ActionParameter) : BaseProperty(field, obj) {
    val button = Button()
    val progress = ProgressIndicator()
    val errorText = Label()
    val box = HBox(button, progress, errorText)

    init {
        progress.isVisible = false
        progress.maxHeight = 20.0

        button.text = annotation.caption
        val block = field.get(obj) as (() -> Unit)

        errorText.isVisible = false
        errorText.textFill = Color.web("#FF0000")

        button.setOnAction {
            progress.isVisible = true
            errorText.isVisible = false

            thread {
                try {
                    block()
                } catch (ex : Exception) {
                    errorText.isVisible = true
                    errorText.text = "${ex.message}"
                } finally {
                    Platform.runLater {
                        progress.isVisible = false
                        if (annotation.invokesChange)
                            propertyChanged.invoke(this)
                    }
                }
            }
        }

        box.spacing = 10.0
        children.add(box)
    }
}