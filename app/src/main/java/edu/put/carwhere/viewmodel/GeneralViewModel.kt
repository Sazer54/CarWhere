package edu.put.carwhere.viewmodel

import PreferenceHelper
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.put.carwhere.WelcomeScreenChoice
import edu.put.carwhere.util.PasswordUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class GeneralViewModel(private val context: Context) : ViewModel() {
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation

    private val _selectedVehicleLocation = MutableStateFlow<LatLng?>(null)
    val selectedVehicleLocation: StateFlow<LatLng?> = _selectedVehicleLocation

    private val preferenceHelper = PreferenceHelper(context)

    private val _isLoggedIn = MutableStateFlow(preferenceHelper.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow(preferenceHelper.email())
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _firebaseUser = MutableStateFlow<edu.put.carwhere.firebase.User?>(null)
    val firebaseUser: StateFlow<edu.put.carwhere.firebase.User?> = _firebaseUser.asStateFlow()

    private val _renderMap = MutableStateFlow(false)
    val renderMap: StateFlow<Boolean> = _renderMap.asStateFlow()

    fun setRenderMap(state: Boolean) {
        _renderMap.value = state
    }

    private val _firebaseUserVehicles = MutableStateFlow<List<edu.put.carwhere.firebase.Vehicle>>(
        emptyList()
    )
    val firebaseUserVehicles: StateFlow<List<edu.put.carwhere.firebase.Vehicle>> =
        _firebaseUserVehicles.asStateFlow()

    private val _welcomeScreenChoice =
        MutableStateFlow<WelcomeScreenChoice?>(WelcomeScreenChoice.HOME)
    val welcomeScreenChoice: StateFlow<WelcomeScreenChoice?> = _welcomeScreenChoice.asStateFlow()

    fun setWelcomeScreenChoice(choice: WelcomeScreenChoice) {
        _welcomeScreenChoice.value = choice
    }

    fun registerUser(email: String, password: String, name: String, onResult: (Boolean) -> Unit) {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        val userId = UUID.randomUUID().toString()
        val user = edu.put.carwhere.firebase.User(
            userId = userId,
            name = name,
            email = email,
            passwordHash = PasswordUtil.hashPassword(password)
        )
        usersRef.child(userId).setValue(user)
        setWelcomeScreenChoice(WelcomeScreenChoice.LOGIN)
        onResult(true)
    }

    fun setFirebaseUserVehicles(vehicles: List<edu.put.carwhere.firebase.Vehicle>) {
        _firebaseUserVehicles.value = vehicles
    }
    fun eraseSelectedVehiclePosition() {
        _selectedVehicleLocation.value = null
    }

    fun setLoggedIn(isLoggedIn: Boolean, email: String?) {
        preferenceHelper.setLoggedIn(isLoggedIn, email, _firebaseUser.value!!.name)
        _isLoggedIn.value = isLoggedIn
    }

    val loading = MutableLiveData(true)
    val vehiclesLoading = MutableLiveData(true)

    fun loginUser(email: String, password: String, onResult: (Boolean) -> Unit) {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        val vehiclesRef = database.getReference("vehicles")
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.forEach { snapshot ->
                        val user = snapshot.getValue(edu.put.carwhere.firebase.User::class.java)
                        if (!PasswordUtil.checkPassword(password, user!!.passwordHash)) {
                            onResult(false)
                            return
                        }
                       _firebaseUser.value = user
                        Log.d("Firebase", "User: $user")

                        _firebaseUserVehicles.value = emptyList()
                        firebaseUser.value?.vehicles?.keys?.forEach { vehicleId ->
                            Log.d("Firebase", "analysing vehicle $vehicleId")
                            vehiclesRef.child(vehicleId).get().addOnSuccessListener {
                                val vehicle =
                                    it.getValue(edu.put.carwhere.firebase.Vehicle::class.java)
                                if (vehicle != null) {
                                    _firebaseUserVehicles.value += vehicle
                                    Log.d("Firebase", "Vehicle of the user: $vehicle")
                                }
                            }
                            vehiclesLoading.value = false
                        }
                    }
                    loading.value = false


                    onResult(true)
                } else {
                    onResult(false)
                    Log.d("Firebase", "User with email $email not found.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "Failed to read users.", databaseError.toException())
            }
        })
        Log.d("Firebase", "loginUser")
    }

    fun loginUserWithoutPassword(email: String) {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        val vehiclesRef = database.getReference("vehicles")
        Log.d("Firebase", "Login user without password")
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.children.forEach { snapshot ->
                        val user = snapshot.getValue(edu.put.carwhere.firebase.User::class.java)
                        _firebaseUser.value = user
                        Log.d("Firebase", "User: $user")
                    }
                } else {
                    Log.d("Firebase", "User with email $email not found.")
                }
                vehiclesLoading.value = true
                _firebaseUserVehicles.value = emptyList()
                if (_firebaseUser.value?.vehicles!!.isEmpty()) {
                    vehiclesLoading.value = false
                }
                firebaseUser.value?.vehicles?.keys?.forEach { vehicleId ->
                    Log.d("Firebase", "analysing vehicle $vehicleId")
                    vehiclesRef.child(vehicleId).get().addOnSuccessListener {
                        val vehicle = it.getValue(edu.put.carwhere.firebase.Vehicle::class.java)
                        if (vehicle != null) {
                            _firebaseUserVehicles.value += vehicle
                            Log.d("Firebase", "Vehicle of the user: $vehicle")
                        }
                    }
                    vehiclesLoading.value = false
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "Failed to read users.", databaseError.toException())
            }
        })
        loading.value = false
    }

    fun setLocation(location: LatLng) {
        _currentLocation.value = location
    }

    fun setSelectedVehicleLocation(location: LatLng) {
        _selectedVehicleLocation.value = location
    }

    fun refetchUserAndVehicles() {
        val database = Firebase.database
        val usersRef = database.getReference("users")
        val vehiclesRef = database.getReference("vehicles")
        Log.d("Firebase", "Refetch user")
        usersRef.orderByChild("email").equalTo(_firebaseUser.value!!.email)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dataSnapshot.children.forEach { snapshot ->
                            val user = snapshot.getValue(edu.put.carwhere.firebase.User::class.java)
                            _firebaseUser.value = user
                            Log.d("Firebase", "Refetched: $user")
                        }
                    } else {
                        Log.d(
                            "Firebase",
                            "User with email ${firebaseUser.value!!.email} not found."
                        )
                    }
                    _firebaseUserVehicles.value = emptyList()
                    firebaseUser.value?.vehicles?.keys?.forEach { vehicleId ->
                        Log.d("Firebase", "analysing vehicle $vehicleId")
                        vehiclesRef.child(vehicleId).get().addOnSuccessListener {
                            val vehicle = it.getValue(edu.put.carwhere.firebase.Vehicle::class.java)
                            if (vehicle != null) {
                                _firebaseUserVehicles.value += vehicle
                                Log.d("Firebase", "Vehicle of the user: $vehicle")
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("Firebase", "Failed to read users.", databaseError.toException())
                }
            })
    }

    fun removeVehicleFromUser(vehicleId: String) {
        val updatedVehicles = _firebaseUserVehicles.value.filterNot { it.vehicleId == vehicleId }
        _firebaseUserVehicles.value = updatedVehicles
    }
}