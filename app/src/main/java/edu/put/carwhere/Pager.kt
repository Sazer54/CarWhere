package edu.put.carwhere

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import edu.put.carwhere.viewmodel.GeneralViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerBottomBar(
    selectedTabIndex: State<Int>,
    scope: CoroutineScope,
    pagerState: PagerState,
    viewModel: GeneralViewModel,
    navController: NavController
) {
    TabRow(
        selectedTabIndex = selectedTabIndex.value,
        modifier = Modifier.fillMaxWidth()
    ) {
        HomeTabs.entries.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTabIndex.value == index,
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.outline,
                onClick = {
                    if (index == 1) {
                        viewModel.eraseSelectedVehiclePosition()
                        viewModel.setRenderMap(true)
                    } else {
                        viewModel.setRenderMap(false)
                    }
                    scope.launch {
                        pagerState.animateScrollToPage(tab.ordinal)
                    }
                },
                text = { Text(text = tab.text) },
                icon = {
                    Icon(
                        imageVector = if (selectedTabIndex.value == index)
                            tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.text
                    )
                }
            )
        }
    }
}


enum class HomeTabs(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val text: String
) {
    Vehicles(
        selectedIcon = Icons.Filled.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar,
        text = "Vehicles"
    ),
    Map(
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
        text = "Map"
    ),
    UserProfile(
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        text = "Profile"
    ),
}