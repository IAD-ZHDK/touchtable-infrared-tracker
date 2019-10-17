package ch.bildspur.ui.properties

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class StringParameter(val name: String, val isEditable: Boolean = true)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextParameter(val name: String, val isEditable: Boolean = true,
                               val width : Double = 200.0, val height : Double = 120.0,
                               val wordWrap : Boolean = false)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SliderParameter(val name: String,
                                 val minValue: Double = 0.0,
                                 val maxValue: Double = 100.0,
                                 val majorTick: Double = 1.0,
                                 val snap: Boolean = true,
                                 val roundInt: Boolean = false)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RangeSliderParameter(val name: String,
                                      val minValue: Double = 0.0,
                                      val maxValue: Double = 100.0,
                                      val majorTick: Double = 1.0,
                                      val snap: Boolean = true,
                                      val roundInt: Boolean = false)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class NumberParameter(val name: String, val unit : String = "")

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class BooleanParameter(val name: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PVectorParameter(val name: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Float2Parameter(val name: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Float3Parameter(val name: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PVectorAngleParameter(val name: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ActionParameter(val name: String, val caption: String, val invokesChange : Boolean = true)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnumParameter(val name: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class LabelParameter(val name: String)