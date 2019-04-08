package com.example.mybluetoothapp

interface Constants {
    companion object {

        // Message types sent from the BluetoothChatService Handler
        const val MESSAGE_READ: Int = 1
        val MESSAGE_WRITE: Int = 2
        val MESSAGE_TOAST: Int = 3

        // Key names received from the BluetoothChatService Handler
        val TOAST = "toast"
    }

}