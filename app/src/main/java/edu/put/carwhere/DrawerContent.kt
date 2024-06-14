package edu.put.carwhere

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.put.carwhere.viewmodel.GeneralViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerContent(
    pagerState: PagerState,
    drawerState: DrawerState,
    scope: CoroutineScope,
    viewModel: GeneralViewModel
) {
    ModalDrawerSheet {
        Box {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = R.drawable.banner_stickmen),
                contentDescription = "logo",
                contentScale = ContentScale.FillWidth
            )
            Text(
                text = "CarWhere",
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = 2.dp, y = 2.dp)
                    .alpha(0.75f),
                color = Color.hsv(0f, 0f, 0.5f),
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Text(
                text = "CarWhere",
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                color = Color.White,
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            NavigationDrawerItem(
                modifier = Modifier.padding(bottom = 10.dp),
                icon = {
                    Icon(Icons.Filled.DirectionsCar, contentDescription = "Car")
                },
                label = { Text(text = "Vehicles") },
                selected = pagerState.currentPage == 0,
                onClick = {
                    if (pagerState.currentPage != 0) {
                        scope.launch {
                            viewModel.setRenderMap(false)
                            pagerState.animateScrollToPage(0)
                        }
                    }
                    if (drawerState.isOpen) scope.launch { drawerState.close() }
                }
            )
            NavigationDrawerItem(
                modifier = Modifier.padding(bottom = 10.dp),
                icon = {
                    Icon(Icons.Filled.Map, contentDescription = "Map")
                },
                label = { Text(text = "Map") },
                selected = pagerState.currentPage == 1,
                onClick = {
                    if (pagerState.currentPage != 1) {
                        scope.launch {
                            viewModel.setRenderMap(true)
                            pagerState.animateScrollToPage(1)
                        }
                    }
                    if (drawerState.isOpen) scope.launch { drawerState.close() }
                }
            )

            NavigationDrawerItem(
                modifier = Modifier.padding(bottom = 10.dp),
                icon = {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Profile")
                },
                label = { Text(text = "Your profile") },
                selected = pagerState.currentPage == 2,
                onClick = {
                    if (pagerState.currentPage != 2) {
                        scope.launch {
                            viewModel.setRenderMap(false)
                            pagerState.animateScrollToPage(2)
                        }
                    }
                    if (drawerState.isOpen) scope.launch { drawerState.close() }
                }
            )
        }
    }
}