package ch.zhdk.tracking.list

inline fun <T> MutableSet<T>.update(items :  List<T>, crossinline onAdd: (T) -> Unit = { }, crossinline onUpdate: (T) -> Unit = { }) {
    // find item sets
    val toRemove = this - items
    val toAdd  = items - this

    this.removeAll(toRemove)
    this.addAll(toAdd)

    toAdd.forEach(onAdd)
    this.forEach(onUpdate)
}