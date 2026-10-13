package com.umbe.appauto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Query("SELECT * FROM cars ORDER BY nome ASC")
    fun getAllCars(): Flow<List<Car>>

    @Query("SELECT * FROM cars")
    suspend fun getAllCarsOnce(): List<Car>

    @Query("SELECT * FROM cars WHERE id = :id")
    suspend fun getCarById(id: Int): Car?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: Car)

    @Update
    suspend fun updateCar(car: Car)

    @Delete
    suspend fun deleteCar(car: Car)
}
