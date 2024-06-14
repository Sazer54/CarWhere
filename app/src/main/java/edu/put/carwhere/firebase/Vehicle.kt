package edu.put.carwhere.firebase

import edu.put.carwhere.database.VehicleType

data class Vehicle(
    val vehicleId: String = "",
    val name: String = "",
    val macAddress: String = "",
    var lastSeenLong: Double? = null,
    var lastSeenLat: Double? = null,
    var lastSeenTimestamp: Long? = null,
    var lastUsedBy: String? = null,
    val type: String = VehicleType.CAR.toString(),
    val users: Map<String, Boolean> = emptyMap()
)
