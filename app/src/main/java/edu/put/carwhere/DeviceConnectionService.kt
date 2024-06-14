package edu.put.carwhere

import PreferenceHelper
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.put.carwhere.util.LocationUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeviceConnectionService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private val bluetoothConnectionReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    // Device has connected
                    // Update your list and location here
                    serviceScope.launch {
                        //updateDeviceLocation(device)
                        Log.d("DEVICE", "Device ${device.name} connected")
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    // Device has disconnected
                    // Update your list and location here
                    serviceScope.launch {
                        updateDeviceLocation(device)
                        Log.d("DEVICE", "Device ${device.name} disconnected")
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        Log.d("XD", "Service started")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CarWhere")
            .setContentText("Keeping track of your bluetooth devices")
            .setSmallIcon(R.drawable.notification_vector)
            .setContentIntent(pendingIntent)
            .addAction(0, "Stop", null)
            .build()

        startForeground(1, notification)

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(bluetoothConnectionReceiver, filter)

        return START_REDELIVER_INTENT
    }

    @SuppressLint("MissingPermission")
    private suspend fun updateDeviceLocation(device: BluetoothDevice) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val database = Firebase.database
        val vehiclesRef = database.getReference("vehicles")
        val location = LocationUtil.getCurrentLocation(fusedLocationClient)
        val context = applicationContext
        val preferenceHelper = PreferenceHelper(context)

        vehiclesRef.orderByChild("macAddress").equalTo(device.address).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val vehicleDataSnapshot = dataSnapshot.children.iterator().next()
                    val vehicleData = vehicleDataSnapshot.getValue(edu.put.carwhere.firebase.Vehicle::class.java)

                    vehicleData?.lastSeenLat = location.latitude
                    vehicleData?.lastSeenLong = location.longitude
                    vehicleData?.lastSeenTimestamp = System.currentTimeMillis()
                    vehicleData?.lastUsedBy = preferenceHelper.name()
                    Log.d("Bluetooth", "Vehicle: $vehicleData")

                    vehiclesRef.child(vehicleDataSnapshot.key!!).setValue(vehicleData)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "Failed to read vehicle.", databaseError.toException())
            }
        })
        Toast.makeText(this@DeviceConnectionService, "Location updated for ${device.name} to be ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Device Connection Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister the receiver
        unregisterReceiver(bluetoothConnectionReceiver)
    }

    companion object {
        const val CHANNEL_ID = "DeviceConnectionServiceChannel"
    }
}