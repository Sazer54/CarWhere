package edu.put.carwhere.database

import android.media.Image
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val macAddress: String,
    val name: String,
    val lastSeenPhoto: String? = null,
    var lastSeenLong: Double? = null,
    var lastSeenLat: Double? = null,
    val currentlyInUse: Boolean = false,
    val type: VehicleType = VehicleType.CAR,
)