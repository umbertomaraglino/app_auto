package com.umbe.appauto.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.umbe.appauto.data.Car
import com.umbe.appauto.data.CarDocument
import com.umbe.appauto.data.KmEntry
import com.umbe.appauto.data.CarViewModel
import com.umbe.appauto.ui.theme.Background
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun InfoTab(car: Car, viewModel: CarViewModel) {
    val context = LocalContext.current
    val kmEntries by viewModel.getKmEntries(car.id).collectAsState(initial = emptyList())
    val documents by viewModel.getDocuments(car.id).collectAsState(initial = emptyList())
    var showKmDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var docName by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val name = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                c.moveToFirst()
                if (idx >= 0) c.getString(idx) else "documento"
            } ?: "documento"
            pendingUri = uri
            docName = name.substringBeforeLast(".")
            showNameDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (car.targa.isNotBlank()) {
            TargaCard(targa = car.targa)
        }

        InfoSection(title = "Veicolo") {
            InfoRow(icon = Icons.Default.Badge, label = "Nome", value = car.nome)
            InfoRow(icon = Icons.Default.DirectionsCar, label = "Marca", value = car.marca)
            InfoRow(icon = Icons.Default.Category, label = "Modello", value = car.modello)
            InfoRow(icon = Icons.Default.LocalGasStation, label = "Carburante", value = car.carburante)
        }

        InfoSection(title = "Chilometraggio") {
            KmRow(
                km = car.kilometraggio,
                entriesCount = kmEntries.size,
                onClick = { showKmDialog = true }
            )
        }

        // Sezione documenti
        DocumentsSection(
            documents = documents,
            onAdd = { filePicker.launch("*/*") },
            onOpen = { doc ->
                val file = File(doc.filePath)
                if (file.exists()) {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, doc.mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Apri con"))
                }
            },
            onDelete = { doc -> viewModel.deleteDocument(doc) }
        )
    }

    if (showKmDialog) {
        KmDialog(
            car = car,
            entries = kmEntries,
            onAddEntry = { km -> viewModel.addKmEntry(car, km) },
            onDeleteEntry = { entry -> viewModel.deleteKmEntry(entry) },
            onDismiss = { showKmDialog = false }
        )
    }

    if (showNameDialog && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Nome documento") },
            text = {
                OutlinedTextField(
                    value = docName,
                    onValueChange = { docName = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addDocument(car.id, docName.ifBlank { "documento" }, pendingUri!!)
                        showNameDialog = false
                        pendingUri = null
                    }
                ) { Text("Salva") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false; pendingUri = null }) { Text("Annulla") }
            }
        )
    }
}

@Composable
private fun KmRow(km: Int, entriesCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Chilometraggio", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            Text(
                text = if (km > 0) formatKm(km) else "Non impostato",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0F172A)
            )
            if (entriesCount > 0) {
                Text(
                    text = "$entriesCount aggiornamenti",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
    }
    HorizontalDivider(modifier = Modifier.padding(start = 66.dp), color = Color(0xFFF1F5F9))
}

@Composable
private fun KmDialog(
    car: Car,
    entries: List<KmEntry>,
    onAddEntry: (Int) -> Unit,
    onDeleteEntry: (KmEntry) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    var newKm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chilometraggio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Inserimento nuovo km
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newKm,
                        onValueChange = { newKm = it.filter { c -> c.isDigit() } },
                        label = { Text("Nuovo km") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        suffix = { Text("km") }
                    )
                    Button(
                        onClick = {
                            val km = newKm.toIntOrNull()
                            if (km != null && km > 0) {
                                onAddEntry(km)
                                newKm = ""
                            }
                        },
                        enabled = newKm.toIntOrNull() != null && (newKm.toIntOrNull() ?: 0) > 0
                    ) {
                        Text("Salva")
                    }
                }

                // Storico
                if (entries.isNotEmpty()) {
                    HorizontalDivider()
                    Text(
                        text = "Storico",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF64748B)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(min = 80.dp, max = 260.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(entries) { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formatKm(entry.km),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = LocalDate.ofEpochDay(entry.date).format(formatter),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteEntry(entry) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Elimina",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Chiudi") }
        }
    )
}

@Composable
private fun TargaCard(targa: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "TARGA", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B), letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEFCE8))
                    .border(2.dp, Color(0xFFCA8A04), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = targa.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = 4.sp,
                    color = Color(0xFF1C1917)
                )
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            letterSpacing = 1.5.sp
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { content() }
        }
    }
}

@Composable
private fun ColumnScope.InfoRow(icon: ImageVector, label: String, value: String) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
        }
    }
    HorizontalDivider(modifier = Modifier.padding(start = 66.dp), color = Color(0xFFF1F5F9))
}

@Composable
private fun DocumentsSection(
    documents: List<CarDocument>,
    onAdd: () -> Unit,
    onOpen: (CarDocument) -> Unit,
    onDelete: (CarDocument) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DOCUMENTI",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.5.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi documento", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
            }
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (documents.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAdd() }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Tocca + per aggiungere un documento", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    documents.forEach { doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpen(doc) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFEEF2FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when {
                                    doc.mimeType.contains("pdf") -> Icons.Default.PictureAsPdf
                                    doc.mimeType.startsWith("image") -> Icons.Default.Image
                                    else -> Icons.Default.InsertDriveFile
                                }
                                Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
                                Text(
                                    LocalDate.ofEpochDay(doc.addedDate).format(formatter),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            IconButton(onClick = { onDelete(doc) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina", modifier = Modifier.size(16.dp), tint = Color(0xFFEF4444))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 66.dp), color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

fun formatKm(km: Int): String = when {
    km >= 1_000 -> "${String.format("%,d", km).replace(',', '.')} km"
    else -> "$km km"
}
