package com.fokot.consistency.model

data class Habit(
    val id: String,
    val name: String,
    val color: Long = 0xFF2196F3.toLong(), // Default blue color
    val type: HabitType = HabitType.BOOLEAN,
    val unit: String = "",
    val entries: Map<String, HabitValue> = emptyMap()
)

enum class HabitType(val displayName: String) {
    BOOLEAN("Yes/No"),             // Simple yes/no completion
    WHOLE_NUMBER("Count (1, 2, 3...)"),      // Whole numbers (e.g., pushups, pages)
    DECIMAL("Measurement (1.5, 2.3...)")     // Decimal numbers (e.g., miles, kilometers)
}

sealed class HabitValue {
    data class BooleanValue(val completed: Boolean) : HabitValue()
    data class NumericValue(
        val value: Double,
        val isWholeNumber: Boolean = false // true for WHOLE_NUMBER type, false for DECIMAL
    ) : HabitValue() {
        val displayValue: String get() = if (isWholeNumber) value.toInt().toString()
                                        else ((value * 10).toInt() / 10.0).toString()
    }
}
