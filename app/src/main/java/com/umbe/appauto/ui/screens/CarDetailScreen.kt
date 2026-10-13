package com.umbe.appauto.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umbe.appauto.data.Car
import com.umbe.appauto.data.CarViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CarDetailScreen(
    carId: Int,
    viewModel: CarViewModel,
    onBack: () -> Unit
) {
    val cars by viewModel.allCars.collectAsState()
    val car = cars.find { it.id == carId }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    if (car == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Custom header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8))
                        )
                    )
                    .statusBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = car.nome.ifBlank { "${car.marca} ${car.modello}".trim().ifBlank { "Auto" } },
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            if (car.targa.isNotBlank()) {
                                Text(
                                    text = car.targa.uppercase(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = Color.White)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Tab row inside header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 0.dp)
                    ) {
                        listOf("Scadenze", "Info auto").forEachIndexed { index, title ->
                            val selected = pagerState.currentPage == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(bottom = 0.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                TextButton(
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = title,
                                        color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 15.sp
                                    )
                                }
                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(3.dp)
                                            .background(Color.White, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    )
                                } else {
                                    Spacer(Modifier.height(3.dp))
                                }
                            }
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ScadenzeTab(car = car, onUpdate = { viewModel.updateCar(it) })
                    1 -> InfoTab(car = car, viewModel = viewModel)
                }
            }
        }
    }

    if (showEditDialog) {
        AddEditCarDialog(
            car = car,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCar ->
                viewModel.updateCar(updatedCar)
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Elimina auto") },
            text = { Text("Sei sicuro di voler eliminare questa auto?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCar(car)
                    showDeleteConfirm = false
                    onBack()
                }) { Text("Elimina", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Annulla") }
            }
        )
    }
}
