package com.umbe.appauto.notifications

import android.content.Context
import androidx.work.*
import com.umbe.appauto.data.CarDatabase
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class DeadlineWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = CarDatabase.getDatabase(applicationContext).carDao()
        val cars = dao.getAllCarsOnce()
        val today = LocalDate.now().toEpochDay()

        for (car in cars) {
            val name = car.nome.ifBlank { "${car.marca} ${car.modello}".trim().ifBlank { "Auto" } }

            checkDeadline(car.id * 10 + 0, name, "Assicurazione", car.scadenzaAssicurazione, today)
            checkDeadline(car.id * 10 + 1, name, "Bollo", car.scadenzaBollo, today)
            checkDeadline(car.id * 10 + 2, name, "Manutenzione", car.scadenzaManutenzione, today)
            checkDeadline(car.id * 10 + 3, name, "Revisione", car.scadenzaRevisione, today)

            // notifica km manutenzione
            if (car.manutenzioneOgniKm > 0 && car.manutenzioneUltimaKm > 0 && car.kilometraggio > 0) {
                val prossima = car.manutenzioneUltimaKm + car.manutenzioneOgniKm
                val mancano = prossima - car.kilometraggio
                val kmLabel = when {
                    mancano <= 0    -> "Manutenzione richiesta! Km superati di ${-mancano} km"
                    mancano <= 200  -> "Manutenzione urgente tra $mancano km"
                    mancano <= 500  -> "Manutenzione tra $mancano km"
                    else            -> null
                }
                if (kmLabel != null) {
                    NotificationHelper.send(applicationContext, car.id * 10 + 4, "Manutenzione · $name", kmLabel)
                }
            }
        }
        return Result.success()
    }

    private fun checkDeadline(notifId: Int, carName: String, tipo: String, epochDay: Long, today: Long) {
        if (epochDay <= 0L) return
        val daysLeft = epochDay - today
        val label = when (daysLeft) {
            90L -> "Scade tra 3 mesi"
            60L -> "Scade tra 2 mesi"
            30L -> "Scade tra 1 mese"
            14L -> "Scade tra 2 settimane"
            7L  -> "Scade tra 1 settimana"
            else -> return
        }
        NotificationHelper.send(
            applicationContext,
            notifId,
            "$tipo · $carName",
            label
        )
    }

    companion object {
        private const val WORK_NAME = "deadline_check_daily"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DeadlineWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
