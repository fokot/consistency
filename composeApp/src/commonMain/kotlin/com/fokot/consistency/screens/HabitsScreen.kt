package com.fokot.consistency.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fokot.consistency.model.*
import com.fokot.consistency.viewmodel.HabitsViewModel
import kotlinx.datetime.*
import kotlinx.coroutines.launch

// First, let's add a helper function to determine if a habit type is numeric
private fun HabitType.isNumeric(): Boolean {
    return this == HabitType.WHOLE_NUMBER || this == HabitType.DECIMAL
}

@Composable
fun NumericInputDialog(
    habitType: HabitType,
    initialValue: String = "",
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    var error by remember { mutableStateOf("") }
    
    fun updateValue(newValue: Double) {
        if (newValue >= 0) {
            val adjustedValue = if (habitType == HabitType.WHOLE_NUMBER) {
                newValue.toInt().toString()
            } else {
                // Work with integers to avoid floating point precision issues
                val scaledValue = (newValue * 10).toInt()
                (scaledValue / 10.0).toString()
            }
            value = adjustedValue
            error = ""
        }
    }

    // Helper function to safely parse and increment/decrement value
    fun adjustValue(increment: Boolean) {
        try {
            val current = value.toDoubleOrNull() ?: 0.0
            if (habitType == HabitType.WHOLE_NUMBER) {
                updateValue(current + (if (increment) 1.0 else -1.0))
            } else {
                // Work with integers to avoid floating point precision issues
                val scaledCurrent = (current * 10).toInt()
                val newScaledValue = scaledCurrent + (if (increment) 1 else -1)
                updateValue(newScaledValue / 10.0)
            }
        } catch (e: NumberFormatException) {
            updateValue(0.0)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Value") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { adjustValue(false) }
                    ) {
                        Text("-", style = MaterialTheme.typography.h5)
                    }
                    
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newValue ->
                            try {
                                val numericValue = newValue.toDouble()
                                updateValue(numericValue)
                            } catch (e: NumberFormatException) {
                                if (newValue.isEmpty()) {
                                    value = ""
                                    error = ""
                                } else {
                                    error = "Please enter a valid number"
                                }
                            }
                        },
                        modifier = Modifier.width(100.dp),
                        textStyle = MaterialTheme.typography.h6.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (habitType == HabitType.WHOLE_NUMBER) 
                                KeyboardType.Number else KeyboardType.Decimal
                        ),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = { adjustValue(true) }
                    ) {
                        Text("+", style = MaterialTheme.typography.h5)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                try {
                    val numericValue = value.toDouble()
                    if (numericValue >= 0) {
                        onConfirm(numericValue)
                        onDismiss()
                    } else {
                        error = "Please enter a positive number"
                    }
                } catch (e: NumberFormatException) {
                    error = "Please enter a valid number"
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun HabitsScreen() {
    val viewModel = remember { HabitsViewModel() }
    val habits = viewModel.habits
    val showDialog = remember { mutableStateOf(false) }
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    
    var startDate by remember {
        mutableStateOf(today.plus(DatePeriod(days = 365)))
    }
    
    var endDate by remember {
        mutableStateOf(today.minus(DatePeriod(days = 365)))
    }
    
    val dates = remember(startDate, endDate) {
        var current = startDate
        val dates = mutableListOf<LocalDate>()
        while (current >= endDate) {
            dates.add(current)
            current = current.minus(DatePeriod(days = 1))
        }
        dates
    }
    
    // Find today's index for initial scroll position
    val initialScrollIndex = remember(dates) {
        dates.indexOfFirst { it.equals(today) }.coerceAtLeast(0)
    }
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollIndex)
    val coroutineScope = rememberCoroutineScope()
    
    // Monitor scroll position and load more dates when needed
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                // Load more future dates when scrolling near the start
                if (index < 10) {
                    startDate = startDate.plus(DatePeriod(days = 30))
                }
                // Load more past dates when scrolling near the end
                if (index > dates.size - 20) {
                    endDate = endDate.minus(DatePeriod(days = 30))
                }
            }
    }
    


    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Fixed column with habit names
            Column(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colors.surface)
            ) {
            // Header
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text("Habits", style = MaterialTheme.typography.subtitle1)
            }

            // Habit names
            habits.forEach { habit ->
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.body1,
                        color = Color(habit.color)
                    )
                }
            }
        }

        // Scrollable date columns
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(dates) { date ->
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .background(
                            if (date == today) MaterialTheme.colors.primary.copy(alpha = 0.1f)
                            else Color.Transparent
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Date header
                    Column(
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfWeek.name.take(3),
                            style = MaterialTheme.typography.caption
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.subtitle2
                        )
                    }

                    // Habit entries for this date
                    habits.forEach { habit ->
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (val value = habit.entries[date.toString()]) {
                                is HabitValue.BooleanValue -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { 
                                                viewModel.toggleHabit(habit.id, date.toString())
                                            }
                                    ) {
                                        Text(
                                            text = if (value.completed) "✓" else "×",
                                            color = if (value.completed) Color(habit.color) else Color.Gray,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                                is HabitValue.NumericValue -> {
                                    var showNumericDialog by remember { mutableStateOf(false) }
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { showNumericDialog = true }
                                    ) {
                                        Column(
                                            modifier = Modifier.align(Alignment.Center),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = value.displayValue,
                                                style = MaterialTheme.typography.body2,
                                                color = Color(habit.color)
                                            )
                                            if (habit.unit.isNotEmpty()) {
                                                Text(
                                                    text = habit.unit,
                                                    style = MaterialTheme.typography.caption,
                                                    color = Color(habit.color)
                                                )
                                            }
                                        }
                                    }
                                    
                                    if (showNumericDialog) {
                                        NumericInputDialog(
                                            habitType = habit.type,
                                            initialValue = value.displayValue,
                                            onConfirm = { newValue ->
                                                viewModel.updateNumericHabit(habit.id, date.toString(), newValue)
                                            },
                                            onDismiss = { showNumericDialog = false }
                                        )
                                    }
                                }
                                null -> {
                                    if (habit.type == HabitType.BOOLEAN) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { 
                                                    viewModel.toggleHabit(habit.id, date.toString())
                                                }
                                        ) {
                                            Text(
                                                text = "×",
                                                color = Color.Gray,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    } else {
                                        var showNumericDialog by remember { mutableStateOf(false) }
                                        
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clickable { showNumericDialog = true }
                                        ) {
                                            Text(
                                                text = "×",
                                                color = Color.Gray,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                        
                                        if (showNumericDialog) {
                                            NumericInputDialog(
                                                habitType = habit.type,
                                                initialValue = "",
                                                onConfirm = { newValue ->
                                                    viewModel.updateNumericHabit(habit.id, date.toString(), newValue)
                                                },
                                                onDismiss = { showNumericDialog = false }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog.value = true },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.Bottom)
        ) {
            Text("+")
        }

        if (showDialog.value) {
            AddHabitDialog(
                onDismiss = { showDialog.value = false },
                onAdd = { name, type, unit ->
                    viewModel.addHabit(
                        Habit(
                            id = (habits.size + 1).toString(),
                            name = name,
                            type = type,
                            unit = unit
                        )
                    )
                    showDialog.value = false
                }
            )
        }

        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showDialog.value = true }
            ) {
                Text("+")
            }
        }
    }

    if (showDialog.value) {
        AddHabitDialog(
            onDismiss = { showDialog.value = false },
            onAdd = { name, type, unit ->
                viewModel.addHabit(
                    Habit(
                        id = (habits.size + 1).toString(),
                        name = name,
                        type = type,
                        unit = unit
                    )
                )
                showDialog.value = false
            }
        )
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, type: HabitType, unit: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(HabitType.BOOLEAN) }
    var unit by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Habit") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Type:", style = MaterialTheme.typography.subtitle1)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == HabitType.BOOLEAN,
                            onClick = { type = HabitType.BOOLEAN }
                        )
                        Text("Yes/No")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == HabitType.WHOLE_NUMBER,
                            onClick = { type = HabitType.WHOLE_NUMBER }
                        )
                        Text("Whole Number")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = type == HabitType.DECIMAL,
                            onClick = { type = HabitType.DECIMAL }
                        )
                        Text("Decimal")
                    }
                }
                if (type.isNumeric()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (e.g., miles, pages)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, type, unit) },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
