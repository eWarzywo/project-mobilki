package com.example.forttask.network

import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private lateinit var socket: Socket
    private var updateEventsCallback: (() -> Unit)? = null
    private var updateChoresCallback: (() -> Unit)? = null
    private var updateBillsCallback: (() -> Unit)? = null
    private var updateShoppingListCallback: (() -> Unit)? = null

    fun initialize(householdId: String) {
        try {
            socket = IO.socket("http://10.90.83.206:3000")

            socket.on(Socket.EVENT_CONNECT) {
                socket.emit("join-household", householdId)
            }

            socket.on("update-events") {
                updateEventsCallback?.invoke()
            }

            socket.on("update-chores") {
                updateChoresCallback?.invoke()
            }

            socket.on("update-bills") {
                updateBillsCallback?.invoke()
            }

            socket.on("update-shopping-list") {
                updateShoppingListCallback?.invoke()
            }

            socket.connect()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun disconnect(householdId: String) {
        socket.emit("leave-household", householdId)
        socket.disconnect()
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
        return ::socket.isInitialized && socket.connected()
    }
}
