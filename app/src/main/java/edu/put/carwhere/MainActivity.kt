package edu.put.carwhere

import GeneralViewModelFactory
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import edu.put.carwhere.screens.WelcomeScreen
import edu.put.carwhere.ui.theme.CarWhereTheme
import edu.put.carwhere.util.LocationUtil
import edu.put.carwhere.viewmodel.GeneralViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarWhereTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Main() {
    val context = LocalContext.current
    val viewModel: GeneralViewModel = viewModel(factory = GeneralViewModelFactory(context))

    /*LaunchedEffect(Unit) {
        DatabaseInitUtil.initFirebaseDatabase()
    }*/

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    Log.d("Main", "Location permission state: ${locationPermissionState.status}")
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status == PermissionStatus.Granted) {
            Log.d("Permission location", "granted")
            val location = LocationUtil.getCurrentLocation(fusedLocationClient)
            viewModel.setLocation(LatLng(location.latitude, location.longitude))
            Log.d("XDD", "Location: ${location.latitude}, ${location.longitude})")
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }
    val bluetoothPermissionState = rememberPermissionState(Manifest.permission.BLUETOOTH)
    LaunchedEffect(bluetoothPermissionState.status) {
        if (bluetoothPermissionState.status == PermissionStatus.Granted) {
            Log.d("Permission bluetooth", "granted")
        } else {
            bluetoothPermissionState.launchPermissionRequest()
        }
    }

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val currentUser by viewModel.firebaseUser.collectAsState()
    if (isLoggedIn && currentUser == null) {
        viewModel.loginUserWithoutPassword(userEmail)
    }
    val serviceIntent = Intent(context, DeviceConnectionService::class.java)
    context.startForegroundService(serviceIntent)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val loading by viewModel.loading.observeAsState()
            if (!isLoggedIn) {
                WelcomeScreen(viewModel = viewModel)
            } else {
                if (loading!!) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    AppNavigator(viewModel = viewModel)
                }
            }
        }
    }
}


