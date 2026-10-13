package com.umbe.appauto.data

import kotlinx.coroutines.flow.Flow

class CarRepository(private val carDao: CarDao) {
    val allCars: Flow<List<Car>> = carDao.getAllCars()

    suspend fun getCarById(id: Int): Car? = carDao.getCarById(id)
    suspend fun insertCar(car: Car) = carDao.insertCar(car)
    suspend fun updateCar(car: Car) = carDao.updateCar(car)
    suspend fun deleteCar(car: Car) = carDao.deleteCar(car)
}
