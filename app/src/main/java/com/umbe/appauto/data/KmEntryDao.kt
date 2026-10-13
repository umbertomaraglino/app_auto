package com.umbe.appauto.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KmEntryDao {
    @Query("SELECT * FROM km_entries WHERE carId = :carId ORDER BY date DESC, km DESC")
    fun getEntriesForCar(carId: Int): Flow<List<KmEntry>>

    @Insert
    suspend fun insert(entry: KmEntry)

    @Delete
    suspend fun delete(entry: KmEntry)
}
