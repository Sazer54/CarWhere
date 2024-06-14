package edu.put.carwhere.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.put.carwhere.R
import edu.put.carwhere.WelcomeScreenChoice
import edu.put.carwhere.viewmodel.GeneralViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(viewModel: GeneralViewModel) {
    val screenChoice = viewModel.welcomeScreenChoice.collectAsState()
    when (screenChoice.value) {
        WelcomeScreenChoice.HOME -> HomePage(viewModel)
        WelcomeScreenChoice.LOGIN -> LoginScreen(viewModel)
        WelcomeScreenChoice.REGISTER -> RegisterScreen(viewModel)
        null -> TODO()
    }

}

@Composable
fun HomePage(viewModel: GeneralViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 100.dp)
                .width(250.dp)
        ) {
            Box(modifier = Modifier.size(250.dp)) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(color = Color.White)
                }
                Image(
                    painterResource(id = R.drawable.logo),
                    contentDescription = "logo",
                    modifier = Modifier
                        .matchParentSize()
                        .align(Alignment.Center)
                )
            }
            Text(
                "CarWhere",
                fontSize = 50.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Button(onClick = {
                viewModel.setWelcomeScreenChoice(WelcomeScreenChoice.LOGIN)
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Log in")
            }
            Button(onClick = { viewModel.setWelcomeScreenChoice(WelcomeScreenChoice.REGISTER) }, modifier = Modifier.fillMaxWidth()) {
                Text("Register")
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: GeneralViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseUser = viewModel.firebaseUser.collectAsState()

    Scaffold(
        topBar = {
            WelcomeTopBar(text = "Login", viewModel = viewModel)
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .imePadding(), // This handles padding when the keyboard is open
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text(
                            "Sign in",
                            fontSize = 50.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Bottom),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.size(100.dp)) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawCircle(color = Color.White)
                            }
                            Image(
                                painterResource(id = R.drawable.logo),
                                contentDescription = "logo",
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
                item {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Button(onClick = {
                        scope.launch {
                            viewModel.loginUser(email, password) { isSuccess ->
                                if (isSuccess) {
                                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT)
                                        .show()
                                    viewModel.setLoggedIn(true, email)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Login failed: Incorrect login or password.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.setLoggedIn(false, null)
                                }
                            }
                        }
                    }) {
                        Text("Login")
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: GeneralViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firebaseUser = viewModel.firebaseUser.collectAsState()

    Scaffold(
        topBar = {
            WelcomeTopBar(text = "Register", viewModel = viewModel)
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .imePadding(), // This handles padding when the keyboard is open
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text(
                            "Sign up",
                            fontSize = 50.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Bottom),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Box(modifier = Modifier.size(100.dp)) {
                            Canvas(modifier = Modifier.matchParentSize()) {
                                drawCircle(color = Color.White)
                            }
                            Image(
                                painterResource(id = R.drawable.logo),
                                contentDescription = "logo",
                                modifier = Modifier
                                    .matchParentSize()
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
                item {
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    TextField(
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        label = { Text("Repeat password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Button(onClick = {
                        scope.launch {
                            val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                            val namePattern = "^[a-zA-Z0-9 ]*$"
                            if (!email.matches(emailPattern.toRegex())) {
                                Toast.makeText(context, "Invalid email format.", Toast.LENGTH_SHORT).show()
                            } else if (!name.matches(namePattern.toRegex())) {
                                Toast.makeText(context, "Name can only contain letters, numbers and spaces.", Toast.LENGTH_SHORT).show()
                            } else if (password != repeatPassword) {
                                Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.registerUser(email, password, name) { isSuccess ->
                                    if (isSuccess) {
                                        Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Registration failed: Incorrect data.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    }) {
                        Text("Register")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeTopBar(
    text: String,
    viewModel: GeneralViewModel
) {
    TopAppBar(title = { Text(text) }, navigationIcon = {
        IconButton(onClick = {
            viewModel.setWelcomeScreenChoice(WelcomeScreenChoice.HOME)
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    })
}