package edu.put.carwhere

import android.bluetooth.BluetoothAdapter
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.put.carwhere.screens.MapScreen
import edu.put.carwhere.screens.ProfileScreen
import edu.put.carwhere.screens.VehiclesScreen
import edu.put.carwhere.viewmodel.GeneralViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppNavigator(viewModel: GeneralViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val selectedTabIndex = remember {
        derivedStateOf { pagerState.currentPage }
    }
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    val navController = rememberNavController()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                pagerState = pagerState, drawerState = drawerState, scope = scope, viewModel = viewModel
            )
        },
        gesturesEnabled = false
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                PagerBottomBar(
                    selectedTabIndex = selectedTabIndex, scope = scope, pagerState = pagerState, viewModel = viewModel, navController = navController
                )
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(modifier = Modifier.fillMaxSize(), state = pagerState, userScrollEnabled = false) { page ->
                    when (page) {
                        0 -> VehiclesScreen(paddingValues = it, drawerState, scope, viewModel, pagerState, bluetoothAdapter!!)
                        1 -> MapScreen(paddingValues = it, drawerState, scope, viewModel)
                        2 -> ProfileScreen(paddingValues = it, drawerState, scope, viewModel)
                    }
                }
            }
            /*NavHost(navController = navController, startDestination = "vehicles") {
                composable("Vehicles") {
                    VehiclesScreen(padding, drawerState, scope, viewModel, pagerState, bluetoothAdapter = bluetoothAdapter!!)
                }
                composable("Map") {
                    MapScreen(padding, drawerState, scope, viewModel)
                }
                composable("Profile") {
                    ProfileScreen(padding, drawerState, scope, viewModel)
                }
            }*/
        }
    }
}