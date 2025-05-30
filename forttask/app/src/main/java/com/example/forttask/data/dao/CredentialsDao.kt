package com.example.forttask.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.example.forttask.data.entity.Credentials
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialsDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insert(credentials: Credentials)

    @Update(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun update(credentials: Credentials)

    @Delete
    suspend fun delete(credentials: Credentials)

    @Query("SELECT * FROM credentials WHERE id = :id")
    fun getCredentials(id: Int): Flow<Credentials>

    @Query("SELECT * FROM credentials")
    fun getAllCredentials(): Flow<List<Credentials>>
}