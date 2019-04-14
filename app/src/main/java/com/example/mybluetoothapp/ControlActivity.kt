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

        m_bluetoothService?.ConnectToDevice(this, m_address)?.execute()

        //control_led_on.setOnClickListener { sendCommand("a") }
        //control_led_off.setOnClickListener { sendCommand("b") }
        control_led_disconnect.setOnClickListener { m_bluetoothService?.disconnect() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(m_bluetoothService != null)
        {
            m_bluetoothService!!.stop()
        }

        m_bluetoothService?.disconnect()
        finish()
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

    /*private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket?.outputStream?.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }*/

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