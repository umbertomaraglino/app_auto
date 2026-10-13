package com.umbe.appauto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.umbe.appauto.data.Car
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val CARBURANTI = listOf("Benzina", "Diesel", "GPL", "Full Hybrid", "Mild Hybrid")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarDialog(
    car: Car?,
    onDismiss: () -> Unit,
    onSave: (Car) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var nome by remember { mutableStateOf(car?.nome ?: "") }
    var marca by remember { mutableStateOf(car?.marca ?: "") }
    var modello by remember { mutableStateOf(car?.modello ?: "") }
    var carburante by remember { mutableStateOf(car?.carburante ?: "") }
    var carburanteExpanded by remember { mutableStateOf(false) }
    var targa by remember { mutableStateOf(car?.targa ?: "") }
    var km by remember { mutableStateOf(if ((car?.kilometraggio ?: 0) > 0) car!!.kilometraggio.toString() else "") }

    fun epochToStr(epoch: Long): String {
        if (epoch <= 0L) return ""
        return LocalDate.ofEpochDay(epoch).format(formatter)
    }

    fun strToEpoch(s: String): Long {
        if (s.isBlank()) return 0L
        return try { LocalDate.parse(s.trim(), formatter).toEpochDay() } catch (e: DateTimeParseException) { 0L }
    }

    var assicurazione by remember { mutableStateOf(epochToStr(car?.scadenzaAssicurazione ?: 0L)) }
    var bollo by remember { mutableStateOf(epochToStr(car?.scadenzaBollo ?: 0L)) }
    var manutenzione by remember { mutableStateOf(epochToStr(car?.scadenzaManutenzione ?: 0L)) }
    var revisione by remember { mutableStateOf(epochToStr(car?.scadenzaRevisione ?: 0L)) }

    var showDatePickerFor by remember { mutableStateOf<String?>(null) }
    val datePickerState = rememberDatePickerState()

    if (showDatePickerFor != null) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerFor = null },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val date = LocalDate.ofEpochDay(millis / 86400000L)
                        val str = date.format(formatter)
                        when (showDatePickerFor) {
                            "assicurazione" -> assicurazione = str
                            "bollo" -> bollo = str
                            "manutenzione" -> manutenzione = str
                            "revisione" -> revisione = str
                        }
                    }
                    showDatePickerFor = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerFor = null }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (car == null) "Aggiungi auto" else "Modifica auto") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome auto") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = { Text("Marca") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = modello,
                    onValueChange = { modello = it },
                    label = { Text("Modello") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Dropdown carburante
                ExposedDropdownMenuBox(
                    expanded = carburanteExpanded,
                    onExpandedChange = { carburanteExpanded = it }
                ) {
                    OutlinedTextField(
                        value = carburante,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Carburante") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = carburanteExpanded,
                        onDismissRequest = { carburanteExpanded = false }
                    ) {
                        CARBURANTI.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    carburante = tipo
                                    carburanteExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targa,
                    onValueChange = { targa = it.uppercase() },
                    label = { Text("Targa") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )
                OutlinedTextField(
                    value = km,
                    onValueChange = { km = it.filter { c -> c.isDigit() } },
                    label = { Text("Chilometraggio") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("km") }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Scadenze", style = MaterialTheme.typography.labelLarge)

                DateField(
                    label = "Assicurazione",
                    value = assicurazione,
                    onValueChange = { assicurazione = it },
                    onPickerClick = { showDatePickerFor = "assicurazione" }
                )
                DateField(
                    label = "Bollo",
                    value = bollo,
                    onValueChange = { bollo = it },
                    onPickerClick = { showDatePickerFor = "bollo" }
                )
                DateField(
                    label = "Manutenzione",
                    value = manutenzione,
                    onValueChange = { manutenzione = it },
                    onPickerClick = { showDatePickerFor = "manutenzione" }
                )
                DateField(
                    label = "Revisione",
                    value = revisione,
                    onValueChange = { revisione = it },
                    onPickerClick = { showDatePickerFor = "revisione" }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Car(
                        id = car?.id ?: 0,
                        nome = nome,
                        marca = marca,
                        modello = modello,
                        carburante = carburante,
                        targa = targa,
                        kilometraggio = km.toIntOrNull() ?: 0,
                        scadenzaAssicurazione = strToEpoch(assicurazione),
                        scadenzaBollo = strToEpoch(bollo),
                        scadenzaManutenzione = strToEpoch(manutenzione),
                        scadenzaRevisione = strToEpoch(revisione)
                    )
                )
            }) { Text("Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}

@Composable
private fun DateField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onPickerClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("gg/mm/aaaa") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            IconButton(onClick = onPickerClick) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Seleziona data")
            }
        }
    )
}
