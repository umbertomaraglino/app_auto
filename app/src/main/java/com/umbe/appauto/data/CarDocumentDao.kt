package com.umbe.appauto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDocumentDao {
    @Query("SELECT * FROM car_documents WHERE carId = :carId ORDER BY addedDate DESC")
    fun getDocumentsForCar(carId: Int): Flow<List<CarDocument>>

    @Insert
    suspend fun insert(document: CarDocument)

    @Delete
    suspend fun delete(document: CarDocument)
}
