package com.umbe.appauto.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CarViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CarDatabase.getDatabase(application)
    private val carDao = db.carDao()
    private val kmEntryDao = db.kmEntryDao()

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
}
