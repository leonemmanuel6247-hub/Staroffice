package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DocumentEntity::class], version = 1, exportSchema = false)
abstract class StarOfficeDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: StarOfficeDatabase? = null

        fun getDatabase(context: Context): StarOfficeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StarOfficeDatabase::class.java,
                    "staroffice_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
