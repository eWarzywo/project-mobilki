package com.example.forttask.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.forttask.data.dao.CredentialsDao
import com.example.forttask.data.entity.Credentials
import android.content.Context
import androidx.room.Room

@Database(
    entities = [Credentials::class],
    version = 1,
    exportSchema = false
)
abstract class CredentialsDatabase : RoomDatabase() {
    abstract fun credentialsDao(): CredentialsDao

    companion object {
        @Volatile
        private var Instance: CredentialsDatabase? = null

        fun getDatabase(context: Context): CredentialsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CredentialsDatabase::class.java,
                    "credentials_database"
                ).build()
                    .also {
                        Instance = it
                    }
            }
        }
    }
}