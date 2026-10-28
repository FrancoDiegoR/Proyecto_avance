package com.smartgas_frontend.shared.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartgas_frontend.iam.presentation.di.IamPresentationModule
import com.smartgas_frontend.iam.presentation.login.LoginScreen
import com.smartgas_frontend.iam.presentation.profile.ProfileScreen
import com.smartgas_frontend.iam.presentation.register.RegisterScreen
import com.smartgas_frontend.iam.presentation.settings.SettingsScreen
import com.smartgas_frontend.incidentdetection.presentation.di.IncidentDetectionPresentationModule
import com.smartgas_frontend.incidentdetection.presentation.history.HistoryScreen
import com.smartgas_frontend.incidentpreventionnotification.presentation.alerts.AlertsScreen
import com.smartgas_frontend.incidentpreventionnotification.presentation.di.IncidentPreventionNotificationPresentationModule
import com.smartgas_frontend.kitchenmonitoring.presentation.di.KitchenMonitoringPresentationModule
import com.smartgas_frontend.kitchenmonitoring.presentation.addsensor.AddSensorScreen
import com.smartgas_frontend.kitchenmonitoring.presentation.sensordetail.SensorDetailScreen
import com.smartgas_frontend.kitchenmonitoring.presentation.sensors.SensorsScreen
import com.smartgas_frontend.kitchenmonitoring.presentation.zonedetail.ZoneDetailScreen
import com.smartgas_frontend.kitchenmonitoring.presentation.zones.ZonesScreen
import com.smartgas_frontend.paymentmanagement.presentation.di.PaymentManagementPresentationModule
import com.smartgas_frontend.paymentmanagement.presentation.plans.PlansScreen
import com.smartgas_frontend.postincidentprocedures.presentation.di.PostIncidentProceduresPresentationModule
import com.smartgas_frontend.postincidentprocedures.presentation.reports.ReportsScreen
import com.smartgas_frontend.shared.data.di.SharedDataModule
import com.smartgas_frontend.shared.i18n.LocalStrings
import com.smartgas_frontend.shared.presentation.di.SharedPresentationModule
import com.smartgas_frontend.shared.presentation.home.HomeScreen
import com.smartgas_frontend.shared.presentation.pages.NotFoundScreen

/** Igual que NavigationItem de MealsCompose. */
data class NavigationItem(
    val icon: ImageVector,
    val title: String,
    val route: String
)

/**
 * Router principal: login, register y app (que requiere sesion).
 */
@Composable
fun AppNavigation() {
    val sessionService = SharedDataModule.getSessionService()
    val navController = rememberNavController()

    // Con sesion entra directo a la app, sin sesion va al login.
    val startDestination = remember { if (sessionService.isLoggedIn()) Routes.APP else Routes.LOGIN }

    val goToApp: () -> Unit = {
        navController.navigate(Routes.APP) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    val logout: () -> Unit = {
        sessionService.clear()
        navController.navigate(Routes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            val viewModel = viewModel { IamPresentationModule.getLoginViewModel() }

            LoginScreen(
                viewModel = viewModel,
                onLoggedIn = goToApp,
                onRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            val viewModel = viewModel { IamPresentationModule.getRegisterViewModel() }

            RegisterScreen(
                viewModel = viewModel,
                onGoToDashboard = goToApp,
                onLogin = { navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } } }
            )
        }

        composable(Routes.APP) {
            Home(onLogout = logout)
        }
    }
}

private fun NavHostController.navigateSection(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME)
        launchSingleTop = true
    }
}

/** Contenido de la app con la barra inferior del diseno (como Home() de MealsCompose). */
@Composable
private fun Home(onLogout: () -> Unit) {
    val strings = LocalStrings.current
    val es = strings.isSpanish
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val navigate: (String) -> Unit = { route -> navController.navigateSection(route) }

    val navigationItems = listOf(
        NavigationItem(icon = Icons.Outlined.Home, title = if (es) "Inicio" else "Home", route = Routes.HOME),
        NavigationItem(icon = Icons.Outlined.GridView, title = if (es) "Zonas" else "Zones", route = Routes.ZONES),
        NavigationItem(icon = Icons.Outlined.Notifications, title = if (es) "Alertas" else "Alerts", route = Routes.ALERTS),
        NavigationItem(icon = Icons.Outlined.History, title = if (es) "Historial" else "History", route = Routes.HISTORY),
        NavigationItem(icon = Icons.Outlined.Settings, title = if (es) "Ajustes" else "Settings", route = Routes.SETTINGS)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { navigationItem ->
                    NavigationBarItem(
                        // Tambien marca la pestana en sus pantallas hijas (ej. app/zones/{zoneId} -> Zones)
                        selected = currentRoute == navigationItem.route ||
                            currentRoute?.startsWith(navigationItem.route + "/") == true,
                        onClick = { navigate(navigationItem.route) },
                        icon = { Icon(navigationItem.icon, contentDescription = navigationItem.title) },
                        label = { Text(navigationItem.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            // Boton + de la pantalla Zones (diseno 05): abre Add sensor (diseno 08)
            if (currentRoute == Routes.ZONES) {
                FloatingActionButton(onClick = { navController.navigate(Routes.addSensor()) }) {
                    Icon(Icons.Outlined.Add, contentDescription = if (es) "Agregar sensor" else "Add sensor")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    val viewModel = viewModel { SharedPresentationModule.getHomeViewModel() }

                    HomeScreen(viewModel = viewModel, onNavigate = navigate)
                }

                composable(Routes.ZONES) {
                    val viewModel = viewModel { KitchenMonitoringPresentationModule.getZonesViewModel() }

                    ZonesScreen(
                        viewModel = viewModel,
                        onNavigate = navigate,
                        // navigate normal (no navigateSection) para que "atras" vuelva a Zones
                        onOpenZone = { zoneId -> navController.navigate(Routes.zoneDetail(zoneId)) }
                    )
                }

                composable(
                    route = Routes.ZONE_DETAIL,
                    arguments = listOf(navArgument(Routes.ZONE_ID_ARG) { type = NavType.IntType })
                ) { entry ->
                    val zoneId = entry.arguments?.getInt(Routes.ZONE_ID_ARG) ?: 0
                    val viewModel = viewModel(key = "zone-$zoneId") {
                        KitchenMonitoringPresentationModule.getZoneDetailViewModel(zoneId)
                    }

                    ZoneDetailScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenSensor = { sensorId -> navController.navigate(Routes.sensorDetail(sensorId)) },
                        onAddSensor = { id -> navController.navigate(Routes.addSensor(id)) }
                    )
                }

                composable(
                    route = Routes.SENSOR_DETAIL,
                    arguments = listOf(navArgument(Routes.SENSOR_ID_ARG) { type = NavType.IntType })
                ) { entry ->
                    val sensorId = entry.arguments?.getInt(Routes.SENSOR_ID_ARG) ?: 0
                    val viewModel = viewModel(key = "sensor-$sensorId") {
                        KitchenMonitoringPresentationModule.getSensorDetailViewModel(sensorId)
                    }

                    SensorDetailScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate(Routes.editSensor(id)) }
                    )
                }

                composable(
                    route = Routes.EDIT_SENSOR,
                    arguments = listOf(navArgument(Routes.SENSOR_ID_ARG) { type = NavType.IntType })
                ) { entry ->
                    val sensorId = entry.arguments?.getInt(Routes.SENSOR_ID_ARG) ?: 0
                    val viewModel = viewModel(key = "edit-sensor-$sensorId") {
                        KitchenMonitoringPresentationModule.getEditSensorViewModel(sensorId)
                    }

                    // Misma pantalla que Add sensor, en modo edicion
                    AddSensorScreen(viewModel = viewModel, onClose = { navController.popBackStack() })
                }

                composable(
                    route = Routes.ADD_SENSOR,
                    arguments = listOf(
                        // -1 = sin zona preseleccionada (los argumentos Int de Navigation no pueden ser null)
                        navArgument(Routes.ZONE_ID_ARG) {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { entry ->
                    val zoneId = entry.arguments?.getInt(Routes.ZONE_ID_ARG)?.takeIf { it >= 0 }
                    val viewModel = viewModel(key = "add-sensor-$zoneId") {
                        KitchenMonitoringPresentationModule.getAddSensorViewModel(zoneId)
                    }

                    AddSensorScreen(viewModel = viewModel, onClose = { navController.popBackStack() })
                }

                composable(Routes.ALERTS) {
                    val viewModel = viewModel { IncidentPreventionNotificationPresentationModule.getAlertsViewModel() }

                    AlertsScreen(viewModel = viewModel)
                }

                composable(Routes.HISTORY) {
                    val viewModel = viewModel { IncidentDetectionPresentationModule.getHistoryViewModel() }

                    HistoryScreen(viewModel = viewModel)
                }

                composable(Routes.SETTINGS) {
                    val viewModel = viewModel { IamPresentationModule.getSettingsViewModel() }

                    SettingsScreen(viewModel = viewModel, onNavigate = navigate, onLogout = onLogout)
                }

                composable(Routes.SENSORS) {
                    val viewModel = viewModel { KitchenMonitoringPresentationModule.getSensorsViewModel() }

                    SensorsScreen(viewModel = viewModel)
                }

                composable(Routes.PROFILE) {
                    val viewModel = viewModel { IamPresentationModule.getProfileViewModel() }

                    ProfileScreen(viewModel = viewModel, onNavigate = navigate, onLogout = onLogout)
                }

                composable(Routes.PLANS) {
                    val viewModel = viewModel { PaymentManagementPresentationModule.getPlansViewModel() }

                    PlansScreen(viewModel = viewModel)
                }

                composable(Routes.REPORTS) {
                    val viewModel = viewModel { PostIncidentProceduresPresentationModule.getReportsViewModel() }

                    ReportsScreen(viewModel = viewModel)
                }

                composable(Routes.NOT_FOUND) {
                    NotFoundScreen(onGoHome = { navigate(Routes.HOME) })
                }
            }
        }
    }
}
