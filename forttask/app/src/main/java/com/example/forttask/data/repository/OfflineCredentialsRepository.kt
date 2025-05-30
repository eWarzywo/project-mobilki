package com.example.forttask.data.repository

import kotlinx.coroutines.flow.Flow
import com.example.forttask.data.entity.Credentials
import com.example.forttask.data.dao.CredentialsDao

class OfflineCredentialsRepository(
    private val credentialsDao: CredentialsDao
) : CredentialsRepository {

    override suspend fun insert(credentials: Credentials) {
        credentialsDao.insert(credentials)
    }

    override suspend fun update(credentials: Credentials) {
        credentialsDao.update(credentials)
    }

    override suspend fun delete(credentials: Credentials) {
        credentialsDao.delete(credentials)
    }

    override fun getCredentials(id: Int): Flow<Credentials?> {
        return credentialsDao.getCredentials(id)
    }

    override fun getAllCredentials(): Flow<List<Credentials>> {
        return credentialsDao.getAllCredentials()
    }
}

