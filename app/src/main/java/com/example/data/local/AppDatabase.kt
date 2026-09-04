package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.GeneratedNumberDao
import com.example.data.local.entity.GeneratedNumberEntity

@Database(entities = [GeneratedNumberEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun generatedNumberDao(): GeneratedNumberDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "call_number_generator.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
