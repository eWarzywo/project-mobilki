package com.example.forttask

import android.app.Application
import com.example.forttask.util.Logger

class FortTaskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        Logger.init(isDebug)
    }
}