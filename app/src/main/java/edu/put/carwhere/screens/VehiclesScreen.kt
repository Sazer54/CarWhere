package edu.put.carwhere.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.put.carwhere.R
import edu.put.carwhere.TopBar
import edu.put.carwhere.database.VehicleType
import edu.put.carwhere.util.LocationUtil
import edu.put.carwhere.viewmodel.GeneralViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class
)
@Composable
fun VehiclesScreen(
    paddingValues: PaddingValues,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: GeneralViewModel,
    pagerState: PagerState,
    bluetoothAdapter: BluetoothAdapter
) {
    val context = LocalContext.current
    var showAddVehicleDialog by remember {
        mutableStateOf(false)
    }
    var pairedDevices by remember {
        mutableStateOf(listOf<BluetoothDevice>())
    }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val currentFirebaseUser = viewModel.firebaseUser.collectAsState()
    Log.d("Firebase", "Vehicles XDDD: ${currentFirebaseUser.value?.vehicles}")

    if (showAddVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showAddVehicleDialog = false },
            title = { Text(text = "Paired Devices") },
            text = {
                LazyColumn {
                    items(pairedDevices) {
                        Box {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clickable {
                                        scope.launch {
                                            val database = Firebase.database
                                            val vehiclesRef = database.getReference("vehicles")
                                            val usersRef = database.getReference("users")
                                            vehiclesRef
                                                .orderByChild("macAddress")
                                                .equalTo(it.address)
                                                .addListenerForSingleValueEvent(object :
                                                    ValueEventListener {
                                                    @SuppressLint("MissingPermission")
                                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                        if (dataSnapshot.exists()) {
                                                            // The vehicle with the given mac address exists in the database
                                                            val vehicle =
                                                                dataSnapshot.children
                                                                    .iterator()
                                                                    .next()
                                                                    .getValue(
                                                                        edu.put.carwhere.firebase.Vehicle::class.java
                                                                    )
                                                            usersRef
                                                                .child(currentFirebaseUser.value!!.userId)
                                                                .child("vehicles")
                                                                .child(vehicle?.vehicleId!!)
                                                                .setValue(true)
                                                                .addOnSuccessListener {
                                                                    Log.d(
                                                                        "Firebase",
                                                                        "Vehicle with id ${vehicle.vehicleId} persisted in the user's map of vehicles."
                                                                    )
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    Log.w(
                                                                        "Firebase",
                                                                        "Failed to persist vehicle in the user's map of vehicles.",
                                                                        e
                                                                    )
                                                                }
                                                            viewModel.refetchUserAndVehicles()
                                                            Log.d(
                                                                "Firebase",
                                                                "Vehicle with mac address ${it.address} exists."
                                                            )
                                                        } else {
                                                            // The vehicle with the given mac address does not exist in the database
                                                            scope.launch {
                                                                val location =
                                                                    LocationUtil.getCurrentLocation(
                                                                        fusedLocationClient
                                                                    )
                                                                viewModel.setLocation(
                                                                    LatLng(
                                                                        location.latitude,
                                                                        location.longitude
                                                                    )
                                                                )
                                                                val newVehicleId =
                                                                    UUID
                                                                        .randomUUID()
                                                                        .toString()

                                                                val newVehicle =
                                                                    edu.put.carwhere.firebase.Vehicle(
                                                                        vehicleId = newVehicleId,
                                                                        macAddress = it.address,
                                                                        name = it.name,
                                                                        users = mapOf(
                                                                            currentFirebaseUser.value!!.userId to true
                                                                        ),
                                                                        lastSeenLat = location.latitude,
                                                                        lastSeenLong = location.longitude
                                                                    )
                                                                vehiclesRef
                                                                    .child(newVehicleId)
                                                                    .setValue(newVehicle)
                                                                    .addOnSuccessListener {
                                                                        Log.d(
                                                                            "Firebase",
                                                                            "New vehicle inserted with ID: $newVehicleId"
                                                                        )
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        Log.w(
                                                                            "Firebase",
                                                                            "Failed to insert new vehicle.",
                                                                            e
                                                                        )
                                                                    }
                                                                usersRef
                                                                    .child(currentFirebaseUser.value!!.userId)
                                                                    .child("vehicles")
                                                                    .child(newVehicle.vehicleId)
                                                                    .setValue(true)
                                                                    .addOnSuccessListener {
                                                                        Log.d(
                                                                            "Firebase",
                                                                            "Vehicle with id ${newVehicle.vehicleId} persisted in the user's map of vehicles."
                                                                        )
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        Log.w(
                                                                            "Firebase",
                                                                            "Failed to persist vehicle in the user's map of vehicles.",
                                                                            e
                                                                        )
                                                                    }
                                                                viewModel.refetchUserAndVehicles()
                                                            }
                                                            Log.d(
                                                                "Firebase",
                                                                "Vehicle with mac address ${it.address} does not exist."
                                                            )
                                                        }
                                                    }

                                                    override fun onCancelled(databaseError: DatabaseError) {
                                                        // Handle possible errors.
                                                        Log.w(
                                                            "Firebase",
                                                            "Failed to read vehicle.",
                                                            databaseError.toException()
                                                        )
                                                    }
                                                })
                                            showAddVehicleDialog = false
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BluetoothConnected,
                                    contentDescription = "Device",
                                    modifier = Modifier.fillMaxHeight()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(start = 10.dp),
                                ) {
                                    Text(
                                        modifier = Modifier.align(Alignment.CenterStart),
                                        text = it.name,
                                        fontSize = 30.sp,
                                        textAlign = TextAlign.Left,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAddVehicleDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        val vehiclesLoading by viewModel.vehiclesLoading.observeAsState()
        if (vehiclesLoading!!) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                topBar = {
                    TopBar(
                        title = "Your vehicles",
                        drawerState = drawerState,
                        scope = scope,
                        bluetoothAdapter = bluetoothAdapter
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            scope.launch {
                                val bluetoothManager =
                                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                                val bluetoothAdapter = bluetoothManager.adapter
                                bluetoothAdapter?.getProfileProxy(
                                    context,
                                    object : BluetoothProfile.ServiceListener {
                                        override fun onServiceConnected(
                                            profile: Int,
                                            proxy: BluetoothProfile
                                        ) {
                                            if (profile == BluetoothProfile.HEADSET) {
                                                pairedDevices = proxy.connectedDevices.toList()
                                                showAddVehicleDialog = true
                                            }
                                        }

                                        override fun onServiceDisconnected(profile: Int) {
                                            if (profile == BluetoothProfile.HEADSET) {
                                                pairedDevices = emptyList()
                                                showAddVehicleDialog = false
                                            }
                                        }
                                    },
                                    BluetoothProfile.HEADSET
                                )
                            }
                        }
                    }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add vehicle",
                            tint = Color.White
                        )
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
            ) { padding ->
                VehiclesList(padding, viewModel, pagerState, scope)
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VehiclesList(
    paddingValues: PaddingValues,
    viewModel: GeneralViewModel,
    pagerState: PagerState,
    scope: CoroutineScope
) {
    val vehicles by viewModel.firebaseUserVehicles.collectAsState()
    val firebaseUser by viewModel.firebaseUser.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        items(vehicles) {
            VehicleItem(it, viewModel, pagerState, scope)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VehicleItem(
    vehicle: edu.put.carwhere.firebase.Vehicle,
    viewModel: GeneralViewModel,
    pagerState: PagerState,
    scope: CoroutineScope
) {
    val iconResId = getVehicleIcon(vehicle.type)
    val context = LocalContext.current
    var showDeleteVehicleDialog by remember { mutableStateOf(false) }
    var showEditVehicleDialog by remember {
        mutableStateOf(false)
    }
    var editedVehicleName by remember { mutableStateOf(vehicle.name) }
    var selectedVehicleType by remember { mutableStateOf(vehicle.type.toString()) }
    val user = viewModel.firebaseUser.collectAsState()
    val userVehicles = viewModel.firebaseUserVehicles.collectAsState()
    val database = Firebase.database

    if (showDeleteVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteVehicleDialog = false },
            title = { Text("Remove ${vehicle.name}") },
            text = { Text("Are you sure you want to remove ${vehicle.name} from the list?") },
            confirmButton = {
                Button(onClick = {
                    val userRef = database.getReference("users").child(user.value!!.userId)

                    userRef.child("vehicles").child(vehicle.vehicleId).removeValue()
                        .addOnSuccessListener {
                            viewModel.refetchUserAndVehicles()
                            showDeleteVehicleDialog = false
                            Log.d("Firebase", "Vehicle ${vehicle.vehicleId} removed from the user.")
                        }.addOnFailureListener { e ->
                        Log.w("Firebase", "Failed to remove vehicle.", e)
                    }

                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteVehicleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showEditVehicleDialog = false },
            title = { Text("Edit ${vehicle.name}") },
            text = {
                Column {
                    TextField(
                        value = editedVehicleName,
                        onValueChange = { editedVehicleName = it },
                        label = { Text("Vehicle Name") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Select Vehicle Type")
                    VehicleType.entries.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedVehicleType = type.toString() }
                        ) {
                            RadioButton(
                                selected = type.toString() == selectedVehicleType,
                                onClick = { selectedVehicleType = type.toString() }
                            )
                            Text(type.name)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val vehiclesRef = database.getReference("vehicles")
                        vehiclesRef.child(vehicle.vehicleId).child("name")
                            .setValue(editedVehicleName)
                        vehiclesRef.child(vehicle.vehicleId).child("type")
                            .setValue(selectedVehicleType)
                        viewModel.refetchUserAndVehicles()
                        showEditVehicleDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEditVehicleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize()
            .height(80.dp)
            .border(1.dp, Color.White, MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = {
                if (vehicle.lastSeenLat != null && vehicle.lastSeenLong != null) {
                    viewModel.setSelectedVehicleLocation(
                        LatLng(
                            vehicle.lastSeenLat!!,
                            vehicle.lastSeenLong!!
                        )
                    )
                    viewModel.setRenderMap(true)
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                } else {
                    Toast
                        .makeText(
                            context,
                            "Vehicle location not available",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            })
    ) {
        Box(
            modifier = Modifier
                .padding(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = "Vehicle Icon",
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterVertically)
                        .fillMaxHeight()
                )
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .weight(10f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = vehicle.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart),
                        textAlign = TextAlign.Left,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .fillMaxHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .clickable(onClick = {
                                showEditVehicleDialog = true
                            })
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .clickable(onClick = {
                                showDeleteVehicleDialog = true
                            })
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .size(40.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .clickable(onClick = {
                                if (vehicle.lastSeenLat != null && vehicle.lastSeenLong != null) {
                                    viewModel.setSelectedVehicleLocation(
                                        LatLng(
                                            vehicle.lastSeenLat!!,
                                            vehicle.lastSeenLong!!
                                        )
                                    )
                                    viewModel.setRenderMap(true)
                                    scope.launch {
                                        pagerState.animateScrollToPage(1)
                                    }
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            "Vehicle location not available",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            })
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Directions,
                            contentDescription = "Go to",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

            }
        }
    }
}

@DrawableRes
fun getVehicleIcon(vehicleTypeString: String): Int {
    return try {
        val vehicleType = VehicleType.valueOf(vehicleTypeString.uppercase())
        when (vehicleType) {
            VehicleType.TRUCK -> R.drawable.truck
            VehicleType.CAR -> R.drawable.car
            VehicleType.MOTORCYCLE -> R.drawable.motorcycle
            VehicleType.BIKE -> R.drawable.bike
        }
    } catch (e: IllegalArgumentException) {
        // Return a default icon or handle the exception as needed
        R.drawable.car
    }
}