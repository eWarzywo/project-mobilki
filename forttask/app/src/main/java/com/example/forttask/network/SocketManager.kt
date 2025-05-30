package com.example.forttask.network

import com.example.forttask.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket

object SocketManager {
    private var socket: Socket? = null
    private var updateEventsCallback: (() -> Unit)? = null
    private var updateChoresCallback: (() -> Unit)? = null
    private var updateBillsCallback: (() -> Unit)? = null
    private var updateShoppingListCallback: (() -> Unit)? = null
    private var isConnected = false

    fun initialize(householdId: String) {
        try {
            disconnect(householdId)

            val full_url = BuildConfig.FULL_URL
            socket = IO.socket(full_url)

            socket?.on(Socket.EVENT_CONNECT) {
                isConnected = true
                socket?.emit("join-household", householdId)
            }

            socket?.on(Socket.EVENT_DISCONNECT) { isConnected = false }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                isConnected = false
                args.forEach { it?.let { arg -> println("Socket connection error: $arg") } }
            }

            socket?.on("update-events") {
                try {
                    updateEventsCallback?.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.on("update-chores") {
                try {
                    updateChoresCallback?.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.on("update-bills") {
                try {
                    updateBillsCallback?.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.on("update-shopping-list") {
                try {
                    updateShoppingListCallback?.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
            isConnected = false
        }
    }
    fun disconnect(householdId: String) {
        try {
            socket?.let { sock ->
                if (sock.connected()) {
                    sock.emit("leave-household", householdId)
                }
                sock.disconnect()
                sock.off() // Remove all listeners
            }
            socket = null
            isConnected = false

            // Clear callbacks to prevent memory leaks
            updateEventsCallback = null
            updateChoresCallback = null
            updateBillsCallback = null
            updateShoppingListCallback = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUpdateEventsCallback(callback: () -> Unit) {
        updateEventsCallback = callback
    }

    fun setUpdateChoresCallback(callback: () -> Unit) {
        updateChoresCallback = callback
    }

    fun setUpdateBillsCallback(callback: () -> Unit) {
        updateBillsCallback = callback
    }

    fun setUpdateShoppingListCallback(callback: () -> Unit) {
        updateShoppingListCallback = callback
    }

    fun isInitialized(): Boolean {
        return socket != null && isConnected
    }

    fun clearCallbacks() {
        updateEventsCallback = null
        updateChoresCallback = null
        updateBillsCallback = null
        updateShoppingListCallback = null
    }
}
