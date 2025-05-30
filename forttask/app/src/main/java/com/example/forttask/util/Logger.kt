package com.example.forttask.util

import android.util.Log
import timber.log.Timber

object Logger {
    private const val LOG_PREFIX = "FortTask_"

    fun init(isDebug: Boolean) {
        if (isDebug) {
            Timber.plant(DebugTree())
        } else {
        }
        Timber.d("Timber initialized")
    }

    private class DebugTree : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val formattedTag = LOG_PREFIX + (tag ?: "")
            
            val threadInfo = if (priority >= Log.INFO) {
                val thread = Thread.currentThread()
                " [${thread.name}]"
            } else {
                ""
            }
            
            super.log(priority, formattedTag, "$threadInfo $message", t)
        }

        override fun createStackElementTag(element: StackTraceElement): String {
            return element.className.substringAfterLast('.')
        }
    }
}