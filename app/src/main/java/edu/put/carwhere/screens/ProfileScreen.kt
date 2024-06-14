package edu.put.carwhere.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import edu.put.carwhere.R
import edu.put.carwhere.TopBar
import edu.put.carwhere.WelcomeScreenChoice
import edu.put.carwhere.viewmodel.GeneralViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: GeneralViewModel
) {
    val currentUser by viewModel.firebaseUser.collectAsState()
    Log.d("Profile", "Current user to display as profile: $currentUser")
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
                TopBar(title = "Your profile", drawerState = drawerState, scope = scope)
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ProfileNamePic(currentUser!!.name, currentUser!!.email)
                    ProfileButtons(viewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileNamePic(username: String, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.pfp),
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp)
        ) {
            Column {
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = username,
                        fontSize = 40.sp,
                        lineHeight = 40.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = email,
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
                        textAlign = TextAlign.Left,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }
    }
}

@Composable
fun ProfileButtons(viewModel: GeneralViewModel) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(top = 10.dp)
    ) {
        LogoutButton(viewModel)
        Spacer(modifier = Modifier.width(10.dp))
        EditButton(viewModel)
        Spacer(modifier = Modifier.width(10.dp))
        DeleteProfileButton(viewModel)
    }
}

@Composable
fun LogoutButton(viewModel: GeneralViewModel) {
    Button(
        onClick = {
            viewModel.setLoggedIn(false, null)
            viewModel.setWelcomeScreenChoice(WelcomeScreenChoice.HOME)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Text(
            text = "Logout",
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun EditButton(viewModel: GeneralViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }
    var scope = rememberCoroutineScope()
    var editedName by remember {
        mutableStateOf(
            viewModel.firebaseUser.value!!.name
        )
    }

    var editedEmail by remember {
        mutableStateOf(
            viewModel.firebaseUser.value!!.email
        )
    }

    val currentUser by viewModel.firebaseUser.collectAsState()
    val database = Firebase.database
    val usersRef = database.getReference("users")

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        usersRef.child(currentUser!!.userId).child("name").setValue(editedName)
                        usersRef.child(currentUser!!.userId).child("email").setValue(editedEmail)
                        Log.d("Profile", "Email: $editedEmail")
                        if (currentUser!!.email != editedEmail) {
                            viewModel.setLoggedIn(false, null)
                        }
                        viewModel.refetchUserAndVehicles()
                        showEditDialog = false
                    }

                }) {
                    Text("Save")
                }
            },
            title = {
                Text(
                    text = "Edit profile",
                )
            },
            text = {
                Column {
                    TextField(value = editedName, onValueChange = {editedName = it}, label = {Text("Name")})
                    Spacer(modifier = Modifier.height(10.dp))
                    TextField(value = editedEmail, onValueChange = {editedEmail = it}, label = {Text("Email")})
                }
            }
        )
    }

    Button(
        onClick = {
            showEditDialog = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Text(
            text = "Edit profile",
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun DeleteProfileButton(viewModel: GeneralViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var scope = rememberCoroutineScope()
    val currentUser by viewModel.firebaseUser.collectAsState()
    val database = Firebase.database
    val usersRef = database.getReference("users")
    val vehiclesRef = database.getReference("vehicles")

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        val vehiclesSnapshot = withContext(Dispatchers.IO) { vehiclesRef.get().await() }
                        val vehicles = vehiclesSnapshot.children

                        // Iterate over each vehicle
                        for (vehicle in vehicles) {
                            // If the vehicle's "users" map contains the current user, remove the user from the map
                            val usersMap = vehicle.child("users").value as? Map<*, *>
                            if (usersMap != null && usersMap.containsKey(currentUser!!.userId)) {
                                vehicle.ref.child("users").child(currentUser!!.userId).removeValue()
                            }
                        }
                        usersRef.child(currentUser!!.userId).removeValue()
                        viewModel.setLoggedIn(false, null)
                        viewModel.setWelcomeScreenChoice(WelcomeScreenChoice.HOME)
                        showDeleteDialog = false
                    }
                }) {
                    Text("Delete")
                }
            },
            title = {
                Text(
                    text = "Delete profile",
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete your profile?",
                )
            }
        )
    }

    Button(
        onClick = {
            showDeleteDialog = true
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier.padding(top = 10.dp)
    ) {
        Text(
            text = "Delete profile",
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}