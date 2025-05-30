package com.example.forttask.data.container

import android.content.Context
import com.example.forttask.data.database.CredentialsDatabase
import com.example.forttask.data.repository.CredentialsRepository
import com.example.forttask.data.repository.OfflineCredentialsRepository

interface AppContainer {
    val credentialsRepository: CredentialsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val credentialsRepository: CredentialsRepository by lazy {
        OfflineCredentialsRepository(CredentialsDatabase.getDatabase(context).credentialsDao())
    }
}