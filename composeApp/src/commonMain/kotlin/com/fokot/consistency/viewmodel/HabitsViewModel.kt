package com.fokot.consistency.viewmodel

import androidx.compose.runtime.mutableStateListOf
import com.fokot.consistency.model.Habit
import com.fokot.consistency.model.HabitType
import com.fokot.consistency.model.HabitValue

class HabitsViewModel {
    private val _habits = mutableStateListOf<Habit>()
    val habits: List<Habit> = _habits

    init {
        // Add sample habits
        addHabit(
            Habit(
                id = "1",
                name = "Wake up early",
                color = 0xFF2196F3.toLong(), // Blue
                type = HabitType.BOOLEAN,
                entries = mapOf(
                    "2025-02-08" to HabitValue.BooleanValue(true),
                    "2025-02-07" to HabitValue.BooleanValue(true),
                    "2025-02-06" to HabitValue.BooleanValue(false)
                )
            )
        )
        addHabit(
            Habit(
                id = "2",
                name = "Run",
                color = 0xFFE91E63.toLong(), // Pink
                type = HabitType.DECIMAL,
                unit = "miles",
                entries = mapOf(
                    "2025-02-08" to HabitValue.NumericValue(0.9, false),
                    "2025-02-07" to HabitValue.NumericValue(1.2, false),
                    "2025-02-06" to HabitValue.NumericValue(1.3, false)
                )
            )
        )
        addHabit(
            Habit(
                id = "3",
                name = "Read books",
                color = 0xFFFF9800.toLong(), // Orange
                type = HabitType.WHOLE_NUMBER,
                unit = "pages",
                entries = mapOf(
                    "2025-02-08" to HabitValue.NumericValue(50.0, true),
                    "2025-02-07" to HabitValue.NumericValue(38.0, true),
                    "2025-02-06" to HabitValue.NumericValue(65.0, true)
                )
            )
        )
    }

    fun addHabit(habit: Habit) {
        _habits.add(habit)
    }
    
    fun toggleHabit(habitId: String, date: String) {
        val habitIndex = _habits.indexOfFirst { it.id == habitId }
        if (habitIndex >= 0) {
            val habit = _habits[habitIndex]
            if (habit.type == HabitType.BOOLEAN) {
                val currentValue = habit.entries[date] as? HabitValue.BooleanValue
                val newValue = HabitValue.BooleanValue(!(currentValue?.completed ?: false))
                updateHabitEntry(habitIndex, date, newValue)
            }
        }
    }
    
    fun updateNumericHabit(habitId: String, date: String, value: Double) {
        val habitIndex = _habits.indexOfFirst { it.id == habitId }
        if (habitIndex >= 0) {
            val habit = _habits[habitIndex]
            if (habit.type == HabitType.WHOLE_NUMBER || habit.type == HabitType.DECIMAL) {
                val isWholeNumber = habit.type == HabitType.WHOLE_NUMBER
                val adjustedValue = if (isWholeNumber) value.toInt().toDouble() else value
                updateHabitEntry(habitIndex, date, HabitValue.NumericValue(adjustedValue, isWholeNumber))
            }
        }
    }
    
    private fun updateHabitEntry(habitIndex: Int, date: String, value: HabitValue) {
        val habit = _habits[habitIndex]
        val newEntries = habit.entries.toMutableMap()
        newEntries[date] = value
        _habits[habitIndex] = habit.copy(entries = newEntries)
    }

    fun toggleHabitCompletion(habitId: String, date: String) {
        val index = _habits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val habit = _habits[index]
            val newEntries = habit.entries.toMutableMap()
            
            when (habit.type) {
                HabitType.BOOLEAN -> {
                    val currentValue = (habit.entries[date] as? HabitValue.BooleanValue)?.completed ?: false
                    newEntries[date] = HabitValue.BooleanValue(!currentValue)
                }
                HabitType.WHOLE_NUMBER -> {
                    val currentValue = (habit.entries[date] as? HabitValue.NumericValue)?.value ?: 0.0
                    newEntries[date] = HabitValue.NumericValue(currentValue + 1.0, true)
                }
                HabitType.DECIMAL -> {
                    val currentValue = (habit.entries[date] as? HabitValue.NumericValue)?.value ?: 0.0
                    newEntries[date] = HabitValue.NumericValue(currentValue + 0.1, false)
                }
            }
            
            _habits[index] = habit.copy(entries = newEntries)
        }
    }
}
