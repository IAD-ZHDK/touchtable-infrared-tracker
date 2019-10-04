package ch.bildspur.ui.properties.types

import ch.bildspur.event.Event
import javafx.scene.layout.Pane
import java.lang.reflect.Field

abstract class BaseProperty(val field: Field, val obj: Any) : Pane() {
    val propertyChanged = Event<BaseProperty>()
}