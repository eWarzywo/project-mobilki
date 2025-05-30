package com.example.forttask

import android.app.Application
import com.example.forttask.data.container.AppContainer
import com.example.forttask.data.container.AppDataContainer

class CredentialsApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
