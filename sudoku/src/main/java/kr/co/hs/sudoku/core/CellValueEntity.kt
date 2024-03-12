package kr.co.hs.sudoku.core

sealed class CellValueEntity<out ValueType>(
    private val value: ValueType?
) {
    abstract fun getValue(): ValueType
    inline fun <reified ValueType, reified T : CellValueEntity<ValueType>> changeType(): T {
        val constructor = T::class.java.getConstructor(
            ValueType::class.java
        )
        return constructor.newInstance(getValue())
    }

    inline fun <reified ValueType, reified T : CellValueEntity<ValueType>> changeType(value: ValueType): T {
        val constructor = T::class.java.getConstructor(
            ValueType::class.java
        )
        return constructor.newInstance(value)
    }

    operator fun invoke() = getValue()

    /**
     * generalize
     */
    object Empty : CellValueEntity<Nothing>(null) {
        override fun getValue() = throw NotImplementedError()
        override fun toString() = "empty value"
    }

    data class Immutable<ValueType>(
        private val value: ValueType
    ) : CellValueEntity<ValueType>(value) {
        override fun getValue() = value
    }

    data class Mutable<ValueType>(
        private var value: ValueType
    ) : CellValueEntity<ValueType>(value) {
        override fun getValue() = value
        fun setValue(value: ValueType) {
            this.value = value
        }
    }
}