package com.umbe.appauto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umbe.appauto.data.Car
import com.umbe.appauto.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScadenzeTab(car: Car, onUpdate: (Car) -> Unit) {
    val today = LocalDate.now().toEpochDay()
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var editingField by remember { mutableStateOf<String?>(null) }
    var showManutenzioneDialog by remember { mutableStateOf(false) }
    var showCatenaDialog by remember { mutableStateOf(false) }
    var showGommeDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Dialog data scadenza (per assicurazione, bollo, revisione)
    if (editingField != null) {
        val currentEpoch = when (editingField) {
            "assicurazione" -> car.scadenzaAssicurazione
            "bollo" -> car.scadenzaBollo
            "revisione" -> car.scadenzaRevisione
            else -> 0L
        }
        val renewYears = if (editingField == "revisione") 2L else 1L
        val renewLabel = if (editingField == "revisione") "Rinnova +2 anni" else "Rinnova +1 anno"
        DatePickerDialog(
            onDismissRequest = { editingField = null },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val base = if (currentEpoch > 0L) LocalDate.ofEpochDay(currentEpoch) else LocalDate.now()
                        onUpdate(fieldUpdate(car, editingField!!, base.plusYears(renewYears).toEpochDay()))
                        editingField = null
                    }) { Text(renewLabel) }
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) onUpdate(fieldUpdate(car, editingField!!, millis / 86400000L))
                        editingField = null
                    }) { Text("Salva") }
                }
            },
            dismissButton = { TextButton(onClick = { editingField = null }) { Text("Annulla") } }
        ) { DatePicker(state = datePickerState) }
    }

    // Dialog manutenzione (data + km)
    if (showManutenzioneDialog) {
        ManutenzioneDialog(
            car = car,
            onUpdate = onUpdate,
            onDismiss = { showManutenzioneDialog = false }
        )
    }

    if (showCatenaDialog) {
        KmOnlyDialog(
            title = "Sostituzione Catena",
            ogniKm = car.catenaOgniKm,
            ultimaKm = car.catenaUltimaKm,
            currentKm = car.kilometraggio,
            onSave = { ogni, ultima -> onUpdate(car.copy(catenaOgniKm = ogni, catenaUltimaKm = ultima)) },
            onFattaAdesso = { onUpdate(car.copy(catenaUltimaKm = car.kilometraggio)) },
            onDismiss = { showCatenaDialog = false }
        )
    }

    if (showGommeDialog) {
        KmOnlyDialog(
            title = "Cambio Gomme",
            ogniKm = car.gommeOgniKm,
            ultimaKm = car.gommeUltimaKm,
            currentKm = car.kilometraggio,
            onSave = { ogni, ultima -> onUpdate(car.copy(gommeOgniKm = ogni, gommeUltimaKm = ultima)) },
            onFattaAdesso = { onUpdate(car.copy(gommeUltimaKm = car.kilometraggio)) },
            onDismiss = { showGommeDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScadenzaCard(label = "Assicurazione", icon = Icons.Default.Security,
            epochDay = car.scadenzaAssicurazione, today = today, formatter = formatter,
            onClick = { editingField = "assicurazione" })

        ScadenzaCard(label = "Bollo", icon = Icons.Default.Receipt,
            epochDay = car.scadenzaBollo, today = today, formatter = formatter,
            onClick = { editingField = "bollo" })

        // Card manutenzione speciale
        ManutenzioneCard(car = car, today = today, formatter = formatter,
            onClick = { showManutenzioneDialog = true })

        ScadenzaCard(label = "Revisione", icon = Icons.Default.FactCheck,
            epochDay = car.scadenzaRevisione, today = today, formatter = formatter,
            onClick = { editingField = "revisione" })

        KmOnlyCard(
            label = "Sostituzione Catena",
            icon = Icons.Default.Link,
            ogniKm = car.catenaOgniKm,
            ultimaKm = car.catenaUltimaKm,
            currentKm = car.kilometraggio,
            onClick = { showCatenaDialog = true }
        )

        KmOnlyCard(
            label = "Cambio Gomme",
            icon = Icons.Default.Autorenew,
            ogniKm = car.gommeOgniKm,
            ultimaKm = car.gommeUltimaKm,
            currentKm = car.kilometraggio,
            onClick = { showGommeDialog = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManutenzioneDialog(car: Car, onUpdate: (Car) -> Unit, onDismiss: () -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (car.scadenzaManutenzione > 0L) car.scadenzaManutenzione * 86400000L else null
    )
    var ogniKmText by remember { mutableStateOf(if (car.manutenzioneOgniKm > 0) car.manutenzioneOgniKm.toString() else "") }
    var ultimaKmText by remember { mutableStateOf(if (car.manutenzioneUltimaKm > 0) car.manutenzioneUltimaKm.toString() else "") }
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val base = if (car.scadenzaManutenzione > 0L) LocalDate.ofEpochDay(car.scadenzaManutenzione) else LocalDate.now()
                        onUpdate(car.copy(scadenzaManutenzione = base.plusYears(1).toEpochDay()))
                        showDatePicker = false
                    }) { Text("Rinnova +1 anno") }
                    TextButton(onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) onUpdate(car.copy(scadenzaManutenzione = millis / 86400000L))
                        showDatePicker = false
                    }) { Text("Salva") }
                }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annulla") } }
        ) { DatePicker(state = datePickerState) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manutenzione") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // --- Sezione data ---
                Text("Scadenza per data", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                val dateText = if (car.scadenzaManutenzione > 0L)
                    LocalDate.ofEpochDay(car.scadenzaManutenzione).format(formatter)
                else "Nessuna data impostata"
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(dateText)
                }

                HorizontalDivider()

                // --- Sezione km ---
                Text("Promemoria per km", style = MaterialTheme.typography.labelMedium, color = Color(0xFF64748B))
                OutlinedTextField(
                    value = ogniKmText,
                    onValueChange = { ogniKmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Ogni X km") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("km") },
                    placeholder = { Text("es. 10000") }
                )
                OutlinedTextField(
                    value = ultimaKmText,
                    onValueChange = { ultimaKmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Ultima manutenzione fatta a") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("km") },
                    placeholder = { Text("es. 40000") }
                )
                Button(
                    onClick = {
                        onUpdate(car.copy(
                            manutenzioneOgniKm = ogniKmText.toIntOrNull() ?: 0,
                            manutenzioneUltimaKm = ultimaKmText.toIntOrNull() ?: 0
                        ))
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ogniKmText.isNotBlank()
                ) { Text("Salva impostazioni km") }

                if (car.kilometraggio > 0) {
                    OutlinedButton(
                        onClick = {
                            onUpdate(car.copy(manutenzioneUltimaKm = car.kilometraggio))
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Fatta adesso (${formatKm(car.kilometraggio)})")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } }
    )
}

@Composable
private fun ManutenzioneCard(car: Car, today: Long, formatter: DateTimeFormatter, onClick: () -> Unit) {
    val daysLeft = if (car.scadenzaManutenzione > 0L) car.scadenzaManutenzione - today else null

    // stato km
    val kmStatus: KmStatus? = if (car.manutenzioneOgniKm > 0) {
        val prossima = car.manutenzioneUltimaKm + car.manutenzioneOgniKm
        val mancano = prossima - car.km
        KmStatus(prossima = prossima, mancano = mancano)
    } else null

    // colore peggiore tra data e km
    val worstDays = daysLeft
    val kmScaduta = kmStatus != null && kmStatus.mancano <= 0
    val kmUrgente = kmStatus != null && kmStatus.mancano in 1..1000
    val kmProsto = kmStatus != null && kmStatus.mancano in 1001..3000

    val (bgColor, iconBg, iconTint, badgeText, badgeBg, badgeTextColor) = when {
        kmScaduta || (worstDays != null && worstDays < 0) -> ScadenzaColors(UrgencyRedBg, UrgencyRed.copy(0.15f), UrgencyRed, "SCADUTA", UrgencyRed, Color.White)
        kmUrgente || (worstDays != null && worstDays == 0L) -> ScadenzaColors(UrgencyRedBg, UrgencyRed.copy(0.15f), UrgencyRed, "URGENTE", UrgencyRed, Color.White)
        kmProsto || (worstDays != null && worstDays <= 30) -> ScadenzaColors(UrgencyOrangeBg, UrgencyOrange.copy(0.15f), UrgencyOrange, "PRESTO", UrgencyOrange, Color.White)
        (worstDays != null && worstDays <= 90) -> ScadenzaColors(UrgencyYellowBg, UrgencyYellow.copy(0.15f), UrgencyYellow, "PRESTO", UrgencyYellow, Color.White)
        (worstDays != null || kmStatus != null) -> ScadenzaColors(UrgencyGreenBg, UrgencyGreen.copy(0.15f), UrgencyGreen, "OK", UrgencyGreen, Color.White)
        else -> ScadenzaColors(Color.White, Color(0xFFF1F5F9), Color(0xFF94A3B8), "—", Color(0xFFF1F5F9), Color(0xFF94A3B8))
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Build, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Manutenzione", style = MaterialTheme.typography.titleMedium, color = Color(0xFF0F172A))
                    if (car.scadenzaManutenzione > 0L) {
                        Text(
                            LocalDate.ofEpochDay(car.scadenzaManutenzione).format(formatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    } else {
                        Text("Tocca per impostare", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    }
                }
                // giorni
                if (daysLeft != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (daysLeft >= 0) "$daysLeft" else "${-daysLeft}",
                            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = iconTint, lineHeight = 30.sp
                        )
                        Text(
                            text = if (daysLeft >= 0) "giorni" else "fa",
                            style = MaterialTheme.typography.labelSmall, color = iconTint.copy(0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 3.dp)
                        ) { Text(badgeText, style = MaterialTheme.typography.labelSmall, color = badgeTextColor, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            // Sezione km (se impostata)
            if (kmStatus != null) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = iconTint.copy(alpha = 0.15f))
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Prossima a", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(formatKm(kmStatus.prossima), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ogni", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(formatKm(car.manutenzioneOgniKm), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Mancano", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(
                            if (kmStatus.mancano > 0) formatKm(kmStatus.mancano) else "Scaduta",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (kmStatus.mancano <= 0) UrgencyRed else if (kmStatus.mancano <= 1000) UrgencyRed else if (kmStatus.mancano <= 3000) UrgencyOrange else UrgencyGreen
                        )
                    }
                }
            } else if (car.manutenzioneOgniKm == 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Tocca per impostare promemoria km",
                    style = MaterialTheme.typography.bodySmall,
                    color = iconTint.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private val Car.km get() = kilometraggio

private data class KmStatus(val prossima: Int, val mancano: Int)

private fun fieldUpdate(car: Car, field: String, epoch: Long): Car = when (field) {
    "assicurazione" -> car.copy(scadenzaAssicurazione = epoch)
    "bollo" -> car.copy(scadenzaBollo = epoch)
    "revisione" -> car.copy(scadenzaRevisione = epoch)
    else -> car
}

@Composable
private fun ScadenzaCard(
    label: String, icon: ImageVector, epochDay: Long,
    today: Long, formatter: DateTimeFormatter, onClick: () -> Unit
) {
    val daysLeft = if (epochDay > 0L) epochDay - today else null
    val dateStr = if (epochDay > 0L) LocalDate.ofEpochDay(epochDay).format(formatter) else null

    val colors = when {
        daysLeft == null -> ScadenzaColors(Color.White, Color(0xFFF1F5F9), Color(0xFF94A3B8), "—", Color(0xFFF1F5F9), Color(0xFF94A3B8))
        daysLeft < 0 -> ScadenzaColors(UrgencyRedBg, UrgencyRed.copy(.15f), UrgencyRed, "SCADUTA", UrgencyRed, Color.White)
        daysLeft == 0L -> ScadenzaColors(UrgencyRedBg, UrgencyRed.copy(.15f), UrgencyRed, "OGGI", UrgencyRed, Color.White)
        daysLeft <= 30 -> ScadenzaColors(UrgencyOrangeBg, UrgencyOrange.copy(.15f), UrgencyOrange, "URGENTE", UrgencyOrange, Color.White)
        daysLeft <= 90 -> ScadenzaColors(UrgencyYellowBg, UrgencyYellow.copy(.15f), UrgencyYellow, "PRESTO", UrgencyYellow, Color.White)
        else -> ScadenzaColors(UrgencyGreenBg, UrgencyGreen.copy(.15f), UrgencyGreen, "OK", UrgencyGreen, Color.White)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(colors.iconBg),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = colors.iconTint, modifier = Modifier.size(26.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, color = Color(0xFF0F172A))
                if (dateStr != null) Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                else Text("Tocca per impostare", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                if (daysLeft != null) {
                    Text(
                        if (daysLeft >= 0) "$daysLeft" else "${-daysLeft}",
                        fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colors.iconTint, lineHeight = 30.sp
                    )
                    Text(
                        if (daysLeft >= 0) "giorni" else "fa",
                        style = MaterialTheme.typography.labelSmall, color = colors.iconTint.copy(.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(colors.badgeBg).padding(horizontal = 8.dp, vertical = 3.dp)
                ) { Text(colors.badgeText, style = MaterialTheme.typography.labelSmall, color = colors.badgeText2, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun KmOnlyCard(
    label: String,
    icon: ImageVector,
    ogniKm: Int,
    ultimaKm: Int,
    currentKm: Int,
    onClick: () -> Unit
) {
    val kmStatus: KmStatus? = if (ogniKm > 0) {
        val prossima = ultimaKm + ogniKm
        val mancano = prossima - currentKm
        KmStatus(prossima = prossima, mancano = mancano)
    } else null

    val kmScaduta = kmStatus != null && kmStatus.mancano <= 0
    val kmUrgente = kmStatus != null && kmStatus.mancano in 1..1000
    val kmProsto = kmStatus != null && kmStatus.mancano in 1001..3000

    val (bgColor, iconBg, iconTint, badgeText, badgeBg, badgeTextColor) = when {
        kmScaduta -> ScadenzaColors(UrgencyRedBg, UrgencyRed.copy(0.15f), UrgencyRed, "SCADUTA", UrgencyRed, Color.White)
        kmUrgente -> ScadenzaColors(UrgencyRedBg, UrgencyRed.copy(0.15f), UrgencyRed, "URGENTE", UrgencyRed, Color.White)
        kmProsto -> ScadenzaColors(UrgencyOrangeBg, UrgencyOrange.copy(0.15f), UrgencyOrange, "PRESTO", UrgencyOrange, Color.White)
        kmStatus != null -> ScadenzaColors(UrgencyGreenBg, UrgencyGreen.copy(0.15f), UrgencyGreen, "OK", UrgencyGreen, Color.White)
        else -> ScadenzaColors(Color.White, Color(0xFFF1F5F9), Color(0xFF94A3B8), "—", Color(0xFFF1F5F9), Color(0xFF94A3B8))
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp)) }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.titleMedium, color = Color(0xFF0F172A))
                    if (kmStatus == null) {
                        Text("Tocca per impostare", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    }
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 3.dp)
                ) { Text(badgeText, style = MaterialTheme.typography.labelSmall, color = badgeTextColor, fontWeight = FontWeight.Bold) }
            }

            if (kmStatus != null) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = iconTint.copy(alpha = 0.15f))
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Prossima a", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(formatKm(kmStatus.prossima), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ogni", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(formatKm(ogniKm), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Mancano", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                        Text(
                            if (kmStatus.mancano > 0) formatKm(kmStatus.mancano) else "Scaduta",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (kmStatus.mancano <= 1000) UrgencyRed else if (kmStatus.mancano <= 3000) UrgencyOrange else UrgencyGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KmOnlyDialog(
    title: String,
    ogniKm: Int,
    ultimaKm: Int,
    currentKm: Int,
    onSave: (ogni: Int, ultima: Int) -> Unit,
    onFattaAdesso: () -> Unit,
    onDismiss: () -> Unit
) {
    var ogniKmText by remember { mutableStateOf(if (ogniKm > 0) ogniKm.toString() else "") }
    var ultimaKmText by remember { mutableStateOf(if (ultimaKm > 0) ultimaKm.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = ogniKmText,
                    onValueChange = { ogniKmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Ogni X km") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("km") },
                    placeholder = { Text("es. 60000") }
                )
                OutlinedTextField(
                    value = ultimaKmText,
                    onValueChange = { ultimaKmText = it.filter { c -> c.isDigit() } },
                    label = { Text("Ultima sostituzione fatta a") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("km") },
                    placeholder = { Text("es. 50000") }
                )
                Button(
                    onClick = {
                        onSave(ogniKmText.toIntOrNull() ?: 0, ultimaKmText.toIntOrNull() ?: 0)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ogniKmText.isNotBlank()
                ) { Text("Salva") }
                if (currentKm > 0) {
                    OutlinedButton(
                        onClick = {
                            onFattaAdesso()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Fatta adesso (${formatKm(currentKm)})")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } }
    )
}

private data class ScadenzaColors(
    val bg: Color, val iconBg: Color, val iconTint: Color,
    val badgeText: String, val badgeBg: Color, val badgeText2: Color
)
