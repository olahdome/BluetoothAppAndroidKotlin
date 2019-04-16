package com.example.mybluetoothapp

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.mybluetoothapp.Constants.Companion.MESSAGE_READ
import com.example.mybluetoothapp.Constants.Companion.MESSAGE_TOAST
import com.example.mybluetoothapp.Constants.Companion.MESSAGE_WRITE
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.provider.Settings.Global.DEVICE_NAME
import android.R.attr.start
import android.R.string.cancel
import android.bluetooth.BluetoothDevice
import android.media.session.PlaybackState.STATE_NONE
import android.R.string.cancel
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.AsyncTask
import org.greenrobot.eventbus.EventBus
import java.util.*


private const val TAG = "MY_APP_DEBUG_TAG"

class BluetoothService(
    // handler that gets info from Bluetooth service
    //private val handler: Handler
) {

    private var mConnectedThread: ConnectedThread? = null

    var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var m_bluetoothSocket: BluetoothSocket? = null
    lateinit var m_progress: ProgressDialog
    lateinit var m_bluetoothAdapter: BluetoothAdapter
    var m_isConnected: Boolean = false

    @Synchronized
    fun stop() {

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
    }

    fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket?.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        //finish()
    }

    fun write(out: ByteArray) {
        // Create temporary object
        val r: ConnectedThread
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            //if (mState !== STATE_CONNECTED) return
            r = mConnectedThread!!
        }
        // Perform the write unsynchronized
        r.write(out)
    }

    inner class ConnectToDevice(val c: Context, val address: String) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(c, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device = m_bluetoothAdapter.getRemoteDevice(address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    m_bluetoothAdapter.cancelDiscovery()
                    m_bluetoothSocket?.connect()
                }
            } catch (e: IOException) {
                m_bluetoothSocket?.close()
                connectSuccess = false
                e.printStackTrace()
            }
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
            connected(m_bluetoothSocket!!)
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket!!.inputStream
        private val mmOutStream: OutputStream = mmSocket!!.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!44
            // itt a BAJ!!!!!!!!!!!! megegyszer lefut, ha van adat, ha nincs, így kitörli, ha nincs adat
            // MEGOLDANI!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                // Send the obtained bytes to the UI activity.
                /*val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1,
                    mmBuffer)
                readMsg.sendToTarget()*/

                val message = String(mmBuffer,0,numBytes)

                EventBus.getDefault().post(DataEvent(message, numBytes))
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                // Send a failure message back to the activity.
                /*val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)*/
                return
            }

            // Share the sent message with the UI activity.
            /*val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer)
            writtenMsg.sendToTarget()*/
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    @Synchronized
    fun connected(socket: BluetoothSocket) {

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket)
        mConnectedThread!!.start()
    }
}