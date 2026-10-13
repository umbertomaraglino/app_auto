package com.umbe.appauto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umbe.appauto.data.Car
import com.umbe.appauto.data.CarViewModel
import com.umbe.appauto.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CarViewModel,
    onCarClick: (Int) -> Unit
) {
    val cars by viewModel.allCars.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8))
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = "Le mie auto",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    if (cars.isNotEmpty()) {
                        Text(
                            text = "${cars.size} ${if (cars.size == 1) "veicolo" else "veicoli"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF1D4ED8),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi auto")
            }
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cars.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cars) { car ->
                        CarListItem(car = car, onClick = { onCarClick(car.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditCarDialog(
            car = null,
            onDismiss = { showAddDialog = false },
            onSave = { car ->
                viewModel.insertCar(car)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = Color(0xFF1E3A8A)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Nessuna auto ancora",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Aggiungi la tua prima auto\nper tenere traccia delle scadenze",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun CarListItem(car: Car, onClick: () -> Unit) {
    val today = LocalDate.now().toEpochDay()
    val deadlines = listOf(
        car.scadenzaAssicurazione,
        car.scadenzaBollo,
        car.scadenzaManutenzione,
        car.scadenzaRevisione
    ).filter { it > 0L }

    val nearestExpiry = deadlines.minOrNull()
    val daysLeft = nearestExpiry?.let { it - today }

    val accentColor = when {
        daysLeft == null -> Color(0xFF94A3B8)
        daysLeft < 0 -> UrgencyRed
        daysLeft <= 30 -> UrgencyOrange
        daysLeft <= 90 -> Color(0xFFD97706)
        else -> Color(0xFF16A34A)
    }

    val statusLabel = when {
        daysLeft == null -> "Nessuna scadenza"
        daysLeft < 0 -> "Scaduta"
        daysLeft == 0L -> "Scade oggi"
        daysLeft <= 30 -> "Scade a breve"
        daysLeft <= 90 -> "Attenzione"
        else -> "In regola"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(88.dp)
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Car icon box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = car.nome.ifBlank { "${car.marca} ${car.modello}".trim().ifBlank { "Auto" } },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0F172A)
                    )
                    if (car.targa.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = car.targa.uppercase(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                }
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(accentColor.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (daysLeft != null && daysLeft >= 0) "${daysLeft}g" else statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
