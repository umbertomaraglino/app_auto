package com.umbe.appauto.data

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

class CarViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CarDatabase.getDatabase(application)
    private val carDao = db.carDao()
    private val kmEntryDao = db.kmEntryDao()
    private val documentDao = db.documentDao()

    val allCars: StateFlow<List<Car>> = carDao.getAllCars().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun insertCar(car: Car) = viewModelScope.launch { carDao.insertCar(car) }
    fun updateCar(car: Car) = viewModelScope.launch { carDao.updateCar(car) }
    fun deleteCar(car: Car) = viewModelScope.launch { carDao.deleteCar(car) }

    fun getKmEntries(carId: Int): Flow<List<KmEntry>> = kmEntryDao.getEntriesForCar(carId)

    fun addKmEntry(car: Car, km: Int) = viewModelScope.launch {
        kmEntryDao.insert(KmEntry(carId = car.id, km = km, date = LocalDate.now().toEpochDay()))
        // aggiorna anche il km corrente dell'auto se è maggiore di quello salvato
        if (km > car.kilometraggio) {
            carDao.updateCar(car.copy(kilometraggio = km))
        }
    }

    fun deleteKmEntry(entry: KmEntry) = viewModelScope.launch { kmEntryDao.delete(entry) }

    fun getDocuments(carId: Int): Flow<List<CarDocument>> = documentDao.getDocumentsForCar(carId)

    fun addDocument(carId: Int, name: String, uri: Uri) = viewModelScope.launch {
        val context = getApplication<Application>()
        val cr = context.contentResolver
        val mimeType = cr.getType(uri) ?: "application/octet-stream"
        val fileName = cr.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            c.moveToFirst()
            if (idx >= 0) c.getString(idx) else "documento"
        } ?: "documento"
        val docsDir = File(context.filesDir, "documents/$carId").also { it.mkdirs() }
        val destFile = File(docsDir, "${System.currentTimeMillis()}_$fileName")
        cr.openInputStream(uri)?.use { input -> destFile.outputStream().use { input.copyTo(it) } }
        documentDao.insert(CarDocument(
            carId = carId,
            name = name,
            fileName = fileName,
            mimeType = mimeType,
            filePath = destFile.absolutePath,
            addedDate = LocalDate.now().toEpochDay()
        ))
    }

    fun deleteDocument(doc: CarDocument) = viewModelScope.launch {
        File(doc.filePath).delete()
        documentDao.delete(doc)
    }
}
