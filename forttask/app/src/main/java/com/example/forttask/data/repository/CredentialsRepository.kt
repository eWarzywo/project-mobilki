package com.example.forttask.data.repository

import com.example.forttask.data.entity.Credentials
import kotlinx.coroutines.flow.Flow

interface CredentialsRepository {
    suspend fun insert(credentials: Credentials)

    suspend fun update(credentials: Credentials)

    suspend fun delete(credentials: Credentials)

    fun getCredentials(id: Int): Flow<Credentials?>

    fun getAllCredentials(): Flow<List<Credentials>>
}