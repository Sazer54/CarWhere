package edu.put.carwhere.firebase

data class User(
    val userId: String = "",
    val name: String = "",
    var email: String = "",
    val passwordHash: String = "",  // Store the hashed password
    val vehicles: Map<String, Boolean> = emptyMap()
)
