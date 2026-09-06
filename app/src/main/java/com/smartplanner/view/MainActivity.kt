package com.smartplanner.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartplanner.controller.AuthController
import com.smartplanner.controller.DashboardController
import com.smartplanner.controller.CheckInController
import com.smartplanner.controller.OnboardingController
import com.smartplanner.controller.PlanController
import com.smartplanner.controller.ProgressController
import com.smartplanner.controller.ProfileController
import com.smartplanner.view.theme.PrimaryBlue
import com.smartplanner.view.theme.SmartPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by ProfileController.isDarkTheme.collectAsState()

            SmartPlannerTheme(darkTheme = isDarkTheme) {
                MainAppScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Explore : Screen("explore", "Explore", { Icon(Icons.Outlined.Home, contentDescription = "Explore", modifier = Modifier.size(24.dp)) })
    object Dashboard : Screen("dashboard", "Dashboard", { Icon(Icons.Outlined.GridView, contentDescription = "Dashboard", modifier = Modifier.size(24.dp)) })
    object CheckIn : Screen("checkin", "Check-In", { Icon(Icons.Outlined.CheckCircle, contentDescription = "Check-In", modifier = Modifier.size(24.dp)) })
    object MyPlan : Screen("manage_plans", "My Plan", { Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "My Plan", modifier = Modifier.size(24.dp)) })
    object Progress : Screen("progress", "Progress", { Icon(Icons.AutoMirrored.Outlined.TrendingUp, contentDescription = "Progress", modifier = Modifier.size(24.dp)) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authController: AuthController = hiltViewModel()
    val dashboardController: DashboardController = hiltViewModel()
    val checkInController: CheckInController = hiltViewModel()
    val onboardingController: OnboardingController = hiltViewModel()
    val planController: PlanController = hiltViewModel()
    val progressController: ProgressController = hiltViewModel()
    val profileController: ProfileController = hiltViewModel()

    val isLoggedIn by authController.isLoggedIn.collectAsState()
    val isDarkTheme by ProfileController.isDarkTheme.collectAsState()
    val profileState by profileController.uiState.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    // Redirect to Dashboard if logged in, otherwise show Login
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            if (currentRoute == "login" || currentRoute == "register") {
                navController.navigate("dashboard") {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            }
        } else {
            if (currentRoute != "login" && currentRoute != "register" && currentRoute != "splash") {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val showBars = isLoggedIn && currentRoute != "onboarding" && currentRoute != "splash" && currentRoute != null

    Scaffold(
        topBar = {
            if (showBars) {
                TopAppBar(
                    title = { SmartPlannerLogo(fontSize = 20.sp, modifier = Modifier.padding(start = 4.dp)) },
                    actions = {
                        // Light/Dark Theme Toggler
                        IconButton(onClick = { profileController.toggleTheme() }) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = "Toggle Theme"
                            )
                        }

                        // Avatar Dropdown Trigger
                        Box {
                            IconButton(onClick = { showMenu = !showMenu }) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "User Menu",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }

                            // Menu items as requested
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { 
                                        Column {
                                            Text(
                                                text = "Signed in as",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                            Text(
                                                text = profileState.user?.email ?: "demo@smartplanner.com",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        }
                                    },
                                    onClick = { },
                                    enabled = false
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Dashboard", fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("dashboard") {
                                            popUpTo(navController.graph.findStartDestination().id)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Profile Settings", fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("profile")
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = "Log Out",
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        ) 
                                    },
                                    onClick = {
                                        showMenu = false
                                        authController.logout()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (showBars) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    val items = listOf(
                        Screen.Explore,
                        Screen.Dashboard,
                        Screen.CheckIn,
                        Screen.MyPlan,
                        Screen.Progress
                    )
                    items.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = screen.icon,
                            label = { 
                                Text(
                                    text = screen.title, 
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            selected = selected,
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                indicatorColor = PrimaryBlue.copy(alpha = 0.15f)
                            ),
                            onClick = {
                                if (screen.route == Screen.CheckIn.route) {
                                    checkInController.loadTodayCheckIns()
                                }
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(
                    onTimeout = {
                        val targetRoute = if (isLoggedIn) "dashboard" else "login"
                        navController.navigate(targetRoute) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("login") {
                LoginScreen(
                    authController = authController,
                    onNavigateToRegister = { navController.navigate("register") },
                    onAuthSuccess = { 
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    authController = authController,
                    onNavigateToLogin = { navController.navigate("login") },
                    onAuthSuccess = { 
                        navController.navigate("dashboard") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                )
            }
            composable("explore") {
                ExploreScreen(
                    onNavigateToDashboard = { navController.navigate("dashboard") }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    dashboardController = dashboardController,
                    onNavigateToOnboarding = { navController.navigate("onboarding") }
                )
            }
            composable("checkin") {
                CheckInScreen(
                    checkInController = checkInController
                )
            }
            composable("manage_plans") {
                ManagePlansScreen(
                    planController = planController,
                    onNavigateToOnboarding = { navController.navigate("onboarding") },
                    onViewPlanSuccess = { navController.navigate("dashboard") }
                )
            }
            composable("progress") {
                ProgressScreen(
                    progressController = progressController
                )
            }
            composable("profile") {
                ProfileScreen(
                    profileController = profileController,
                    onLogoutSuccess = { authController.logout() }
                )
            }
            composable("onboarding") {
                OnboardingScreen(
                    onboardingController = onboardingController,
                    onComplete = { 
                        navController.navigate("dashboard") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
