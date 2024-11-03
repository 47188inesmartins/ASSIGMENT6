package com.example.assignment4.localStore

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PhotoEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase(){
    abstract fun photoDao() : PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
               Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database-name"
                ) .fallbackToDestructiveMigration()
                   .build()
                   .also { INSTANCE = it }
            }
        }
    }
}