package com.umbe.appauto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "car_documents")
data class CarDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val carId: Int,
    val name: String,
    val fileName: String,
    val mimeType: String,
    val filePath: String,
    val addedDate: Long
)
