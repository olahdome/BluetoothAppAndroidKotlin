package com.example.mybluetoothapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_control.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.Double.parseDouble
import java.lang.NumberFormatException


class ControlActivity: AppCompatActivity() {

    companion object {
        lateinit var m_address: String
        var m_bluetoothService: BluetoothService? = null
        var data: String? = null
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

        control_led_on.setOnClickListener { sendMessage(readEditText.text.toString()) }
        //control_led_on.setOnClickListener { sendCommand("a") }

        //control_led_off.setOnClickListener { sendCommand("b") }
        control_led_disconnect.setOnClickListener { m_bluetoothService?.disconnect() }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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

    private fun setupBluetooth(){
        m_bluetoothService = BluetoothService()
    }

    @Subscribe
    fun onMessageEvent(event: DataEvent) {
        /*var numeric = true
        if (event.data[event.data.lastIndex] == ';')
        {
            try {
                val doubleData = parseDouble(event.data)
            } catch (e: NumberFormatException) {
                numeric = false
            }
            if (numeric) {
            }
        }*/
        //sendMessage("SEND_DATA;")

        readDataTV.text = event.data
        readNumberOfBytesTV.text = event.dataNumberOfBytes.toString()
    }

    private fun sendMessage(message: String){
        /*if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show()
            return
        }*/
        if (message.isNotEmpty()) {
            // Get the message bytes and tell the BluetoothChatService to write
            val send = message.toByteArray()
            m_bluetoothService?.write(send)
        }
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
    }*/

}