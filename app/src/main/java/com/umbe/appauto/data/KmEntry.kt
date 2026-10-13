package com.umbe.appauto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "km_entries")
data class KmEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carId: Int,
    val km: Int,
    val date: Long // epoch day
)
