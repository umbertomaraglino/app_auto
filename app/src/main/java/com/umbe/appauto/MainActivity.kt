package com.umbe.appauto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.umbe.appauto.data.CarViewModel
import com.umbe.appauto.notifications.DeadlineWorker
import com.umbe.appauto.notifications.NotificationHelper
import com.umbe.appauto.ui.screens.AppNavigation
import com.umbe.appauto.ui.theme.AppAutoTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // permesso concesso o negato — il worker è già pianificato
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createChannel(this)
        DeadlineWorker.schedule(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            AppAutoTheme {
                val carViewModel: CarViewModel = viewModel()
                AppNavigation(viewModel = carViewModel)
            }
        }
    }
}
