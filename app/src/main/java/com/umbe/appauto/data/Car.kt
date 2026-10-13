package com.umbe.appauto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class Car(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String = "",
    val marca: String = "",
    val modello: String = "",
    val carburante: String = "",
    val targa: String = "",
    val kilometraggio: Int = 0,
    val scadenzaAssicurazione: Long = 0L,
    val scadenzaBollo: Long = 0L,
    val scadenzaManutenzione: Long = 0L,
    val scadenzaRevisione: Long = 0L,
    // manutenzione km-based
    val manutenzioneOgniKm: Int = 0,
    val manutenzioneUltimaKm: Int = 0
)
