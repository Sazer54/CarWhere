package edu.put.carwhere.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import edu.put.carwhere.R
import edu.put.carwhere.TopBar
import edu.put.carwhere.util.LocationUtil
import edu.put.carwhere.viewmodel.GeneralViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun MapScreen(
    paddingValues: PaddingValues,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: GeneralViewModel
) {
    val context = LocalContext.current
    val fusedContext = remember { LocationServices.getFusedLocationProviderClient(context) }
    LocationUtil.getCurrentLocationNoSuspend(fusedContext) {
        viewModel.setLocation(LatLng(it.latitude, it.longitude))
    }
    val currentLocation by viewModel.currentLocation.collectAsState()
    val selectedVehicleLocation by viewModel.selectedVehicleLocation.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    Log.d("camera", selectedVehicleLocation.toString())
    if (selectedVehicleLocation != null) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedVehicleLocation!!, 17f)
    } else if(currentLocation != null) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
    }
    viewModel.refetchUserAndVehicles()
    val vehicles by viewModel.firebaseUserVehicles.collectAsState()
    val user by viewModel.firebaseUser.collectAsState()
    val renderMap by viewModel.renderMap.collectAsState()

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .background(Color.White),
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            topBar = {
                TopBar(title = "Map", drawerState = drawerState, scope = scope)
            }
        ) {
            if (renderMap) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                    cameraPositionState = cameraPositionState
                ) {
                    currentLocation?.let { location ->
                        val markerState = rememberMarkerState(position = location)
                        markerState.position = location
                        Marker(
                            state = markerState,
                            title = user?.name,
                            snippet = "You are here"
                        )
                    }
                    vehicles.forEach { vehicle ->
                        if (vehicle.lastSeenLong != null && vehicle.lastSeenLat != null) {
                            val position = LatLng(vehicle.lastSeenLat!!, vehicle.lastSeenLong!!)
                            val markerState = rememberMarkerState(position = position)
                            markerState.position = position
                            val snippetInfo = if (vehicle.lastSeenTimestamp != null) {
                                val lastSeenSeconds = (System.currentTimeMillis() - vehicle.lastSeenTimestamp!!) / 1000
                                snippetText(lastSeenSeconds, vehicle.lastUsedBy ?: "Unknown")
                            } else {
                                "No time data available"
                            }

                            Marker(
                                icon = bitmapDescriptorFromVector(LocalContext.current, R.drawable.blue_marker),
                                state = markerState,
                                title = vehicle.name,
                                snippet = snippetInfo
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
    vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true)
    return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
}

fun snippetText(seconds: Long, name: String): String {
    val basis = when {
        seconds < 60 -> "Left $seconds seconds ago"
        seconds < 3600 -> "Left ${seconds / 60} minutes ago"
        seconds < 86400 -> "Left ${seconds / 3600} hours ago"
        else -> "Left ${seconds / 86400} days ago"
    }
    return "$basis by $name"
}
