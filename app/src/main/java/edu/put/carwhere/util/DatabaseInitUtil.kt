package edu.put.carwhere.util

import android.util.Log
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.mindrot.jbcrypt.BCrypt

object DatabaseInitUtil {
    private val vehicleUUIDs = listOf("1111-0000-0000-1111", "2222-0000-0000-2222")
    private val userUUIDs = listOf("1111-1111-1111-1111", "2222-2222-2222-2222")

    private fun getSampleFirebaseUsers(): List<edu.put.carwhere.firebase.User> {
        return listOf(
            edu.put.carwhere.firebase.User(
                userId = userUUIDs[0],
                name = "John Doe",
                email = "xd",
                passwordHash = BCrypt.hashpw("xd", BCrypt.gensalt()),
                vehicles = mapOf(vehicleUUIDs[0] to true, vehicleUUIDs[1] to true)
            ),
            edu.put.carwhere.firebase.User(
                userId = userUUIDs[1],
                name = "Jane Smith",
                vehicles = mapOf(vehicleUUIDs[0] to true)
            )
        )
    }

    private fun getSampleFirebaseVehicles(): List<edu.put.carwhere.firebase.Vehicle> {
        return listOf(
            edu.put.carwhere.firebase.Vehicle(
                vehicleId = vehicleUUIDs[0],
                macAddress = "00:11:22:33:44:55",
                name = "Car",
                users = mapOf(userUUIDs[0] to true),
                lastSeenLat = 52.430924,
                lastSeenLong = 16.943122
            ),
            edu.put.carwhere.firebase.Vehicle(
                vehicleId = vehicleUUIDs[1],
                macAddress = "00:11:22:33:44:66",
                name = "Truck",
                users = mapOf(userUUIDs[0] to true, userUUIDs[1] to true),
                lastSeenLat = 52.431746,
                lastSeenLong = 16.939392,
            )
        )
    }

    fun initFirebaseDatabase() {
        val db = Firebase.database
        val rootRef = db.getReference()

        rootRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase", "Successfully removed all values from the database.")
            } else {
                Log.w("Firebase", "Failed to remove values from the database.", task.exception)
            }
        }

        val usersRef = db.getReference("users")
        val vehiclesRef = db.getReference("vehicles")

        val sampleUsers = getSampleFirebaseUsers()
        val sampleVehicles = getSampleFirebaseVehicles()

        sampleUsers.forEach {
            usersRef.child(it.userId).setValue(it)
        }

        sampleVehicles.forEach {
            vehiclesRef.child(it.vehicleId).setValue(it)
        }

    }
}