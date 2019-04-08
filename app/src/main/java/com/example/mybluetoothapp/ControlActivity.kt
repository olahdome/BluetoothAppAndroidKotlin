package com.example.mybluetoothapp

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import java.util.*
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.android.synthetic.main.activity_control.*
import org.jetbrains.anko.toast
import java.io.IOException
import android.widget.Toast
import android.provider.Settings.Global.DEVICE_NAME
import android.media.session.PlaybackState.STATE_NONE
import android.os.Handler
import android.os.Message
import android.text.method.TextKeyListener.clear
import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.widget.EditText


class ControlActivity: AppCompatActivity() {

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        var m_bluetoothService: BluetoothService? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)

        if(m_bluetoothService == null)
        {
            setupBluetooth()
        }

        ConnectToDevice(this).execute()

        control_led_on.setOnClickListener { sendCommand("a") }
        control_led_off.setOnClickListener { sendCommand("b") }
        control_led_disconnect.setOnClickListener { disconnect() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(m_bluetoothService != null)
        {
            m_bluetoothService!!.stop()
        }

    }

    fun setupBluetooth(){
        m_bluetoothService = BluetoothService(m_handler)
    }
/*
        control_send_butt.setOnClickListener { sendText() }
        control_disconnect_butt.setOnClickListener { disconnect() }
    }


    private fun sendText() {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(et_control.text.toString().toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }*/

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket?.outputStream?.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket?.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(val c:Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(c, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothAdapter.cancelDiscovery()
                    m_bluetoothSocket?.connect()
                }
            } catch (e: IOException) {
                m_bluetoothSocket?.close()
                connectSuccess = false
                e.printStackTrace()
            }

            m_bluetoothService?.connected(m_bluetoothSocket!!)

            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }

    private val m_handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                /*Constants.MESSAGE_WRITE -> {
                    val writeBuf = msg.obj as ByteArray
                    // construct a string from the buffer
                    val writeMessage = String(writeBuf)
                    mConversationArrayAdapter.add(writeMessage)
                }*/
                Constants.MESSAGE_READ -> {
                    val readBuf = msg.obj as ByteArray
                    // construct a string from the valid bytes in the buffer
                    val readMessage = String(readBuf, 0, msg.arg1)
                    readEditText.setText(readMessage)
                }
            }
        }
    }

}