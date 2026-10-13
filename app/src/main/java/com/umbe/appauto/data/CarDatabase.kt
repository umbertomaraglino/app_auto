package com.umbe.appauto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `km_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `carId` INTEGER NOT NULL, `km` INTEGER NOT NULL, `date` INTEGER NOT NULL)"
        )
        database.execSQL("ALTER TABLE `cars` ADD COLUMN `manutenzioneOgniKm` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `cars` ADD COLUMN `manutenzioneUltimaKm` INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `cars` ADD COLUMN `catenaOgniKm` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `cars` ADD COLUMN `catenaUltimaKm` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `cars` ADD COLUMN `gommeOgniKm` INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `cars` ADD COLUMN `gommeUltimaKm` INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(entities = [Car::class, KmEntry::class], version = 3, exportSchema = false)
abstract class CarDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun kmEntryDao(): KmEntryDao

    companion object {
        @Volatile
        private var INSTANCE: CarDatabase? = null

        fun getDatabase(context: Context): CarDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CarDatabase::class.java,
                    "car_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
