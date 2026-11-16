// #member1
// Every screen and route in the app.
package com.example.mini.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mini.data.UserSession
import com.example.mini.screen.RESIDENT_HOME_ROUTE
import com.example.mini.screen.member1.module1.EditResidentScreen
import com.example.mini.screen.member1.module1.LoginScreen
import com.example.mini.screen.member1.module1.RegisterScreen
import com.example.mini.screen.member1.module1.RegistrationApprovalScreen
import com.example.mini.screen.member1.module1.ResidentDetailScreen
import com.example.mini.screen.member1.module1.ResidentHomeScreen
import com.example.mini.screen.member1.module1.ResidentHouseholdScreen
import com.example.mini.screen.member1.module1.ResidentListScreen
import com.example.mini.screen.member1.module1.SecurityHomeScreen
import com.example.mini.screen.member1.module1.StaffHomeScreen
import com.example.mini.screen.member1.module2.VerifyVisitorScreen
import com.example.mini.screen.member1.module2.VisitorExitScreen
import com.example.mini.screen.member1.module2.VisitorFormScreen
import com.example.mini.screen.member1.module2.VisitorPassScreen
import com.example.mini.screen.member1.module2.VisitorRecordsScreen
import com.example.mini.screen.member1.module2.VisitorScreen
import com.example.mini.screen.member1.module2.VisitorSecurityScreen
import com.example.mini.screen.member2.module3.AnnouncementDetailScreen
import com.example.mini.screen.member2.module3.AnnouncementFormScreen
import com.example.mini.screen.member2.module3.CommunityEngagementScreen
import com.example.mini.screen.member2.module3.EventDetailScreen
import com.example.mini.screen.member2.module3.EventFormScreen
import com.example.mini.screen.member2.module3.InfoFormScreen
import com.example.mini.screen.member2.module3.NotificationScreen
import com.example.mini.screen.member2.module4.FacilityBookingScreen
import com.example.mini.screen.member2.module4.FacilityFormScreen
import com.example.mini.screen.member2.module4.FacilityMaintenanceScreen
import com.example.mini.screen.member2.module4.MaintenanceDetailScreen
import com.example.mini.screen.member2.module4.MaintenanceFormScreen
import com.example.mini.screen.member3.module5.ComplaintDetailScreen
import com.example.mini.screen.member3.module5.ComplaintFeedbackScreen
import com.example.mini.screen.member3.module5.ComplaintFormScreen
import com.example.mini.screen.member3.module5.FeedbackFormScreen
import com.example.mini.screen.member3.module6.ChatInboxScreen
import com.example.mini.screen.member3.module6.ChatThreadScreen
import com.example.mini.screen.member3.module6.CommunityEmergencyScreen
import com.example.mini.screen.member3.module6.EmergencyAlertsScreen
import com.example.mini.screen.member3.module6.EmergencyContactFormScreen
import com.example.mini.screen.member3.module6.EmergencyContactsScreen
import com.example.mini.screen.member3.module6.EmergencySosScreen
import com.example.mini.screen.member3.module6.NewPostScreen
import com.example.mini.screen.member3.module6.PostDetailScreen
import com.example.mini.screen.member3.module6.PostReportsScreen

// screens that work without being logged in
private val publicRoutes = setOf("login", "register", "visitor")

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    // If Android kills the app in the background, the back stack is restored but UserSession
    // (in memory) is empty, so a restored page would show nothing. Send the user back to Login.
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    LaunchedEffect(currentRoute, UserSession.currentUserId) {
        if (currentRoute != null && currentRoute !in publicRoutes && UserSession.currentUserId == null) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("visitor") { VisitorScreen(navController) }

        // One home per role
        composable(RESIDENT_HOME_ROUTE) { ResidentHomeScreen(navController) }
        composable("staff_home") { StaffHomeScreen(navController) }
        composable("security_home") { SecurityHomeScreen(navController) }

        // ---------- Member 1 -- Module 1: Resident & Household ----------
        composable("register") { RegisterScreen(navController) }
        composable("resident_household") { ResidentHouseholdScreen(navController) }
        composable(
            route = "edit_resident/{residentId}",
            arguments = listOf(navArgument("residentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val residentId = backStackEntry.arguments?.getInt("residentId") ?: -1
            EditResidentScreen(residentId = residentId, navController = navController)
        }
        composable("staff_registrations") { RegistrationApprovalScreen(navController) }
        composable("staff_residents") { ResidentListScreen(navController) }
        composable(
            route = "staff_resident_detail/{residentId}",
            arguments = listOf(navArgument("residentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val residentId = backStackEntry.arguments?.getInt("residentId") ?: -1
            ResidentDetailScreen(residentId = residentId, navController = navController)
        }

        // ---------- Member 1 -- Module 2: Visitor & Security ----------
        composable("visitor_security") { VisitorSecurityScreen(navController) }
        composable(
            route = "visitor_form?visitorId={visitorId}",
            arguments = listOf(navArgument("visitorId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            val visitorId = backStackEntry.arguments?.getInt("visitorId") ?: -1
            VisitorFormScreen(visitorId = visitorId, navController = navController)
        }
        composable(
            route = "visitor_pass/{visitorId}",
            arguments = listOf(navArgument("visitorId") { type = NavType.IntType })
        ) { backStackEntry ->
            val visitorId = backStackEntry.arguments?.getInt("visitorId") ?: -1
            VisitorPassScreen(visitorId = visitorId, navController = navController)
        }
        composable("verify_visitor") { VerifyVisitorScreen(navController) }
        composable("visitor_exit") { VisitorExitScreen(navController) }
        composable("visitor_records") { VisitorRecordsScreen(navController) }

        // ---------- Member 2 -- Module 3: Community Engagement ----------
        composable(
            route = "community_engagement?tab={tab}",
            arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            CommunityEngagementScreen(initialTab = backStackEntry.arguments?.getInt("tab") ?: 0, navController = navController)
        }
        composable(
            route = "announcement/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            AnnouncementDetailScreen(announcementId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable(
            route = "announcement_form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            AnnouncementFormScreen(announcementId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable(
            route = "event/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            EventDetailScreen(eventId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable(
            route = "event_form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            EventFormScreen(eventId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable(
            route = "info_form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            InfoFormScreen(infoId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable("notifications") { NotificationScreen(navController) }

        // ---------- Member 2 -- Module 4: Facility & Maintenance ----------
        composable(
            route = "facility_maintenance?tab={tab}",
            arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            FacilityMaintenanceScreen(initialTab = backStackEntry.arguments?.getInt("tab") ?: 0, navController = navController)
        }
        composable(
            route = "facility_booking/{facilityId}?bookingId={bookingId}",
            arguments = listOf(
                navArgument("facilityId") { type = NavType.IntType },
                navArgument("bookingId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            FacilityBookingScreen(
                facilityId = backStackEntry.arguments?.getInt("facilityId") ?: -1,
                bookingId = backStackEntry.arguments?.getInt("bookingId") ?: -1,
                navController = navController
            )
        }
        composable(
            route = "facility_form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            FacilityFormScreen(facilityId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable("maintenance_form") { MaintenanceFormScreen(navController) }
        composable(
            route = "maintenance/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            MaintenanceDetailScreen(requestId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }

        // ---------- Member 3 -- Module 5: Complaint & Feedback ----------
        composable(
            route = "complaint_feedback?tab={tab}",
            arguments = listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            ComplaintFeedbackScreen(initialTab = backStackEntry.arguments?.getInt("tab") ?: 0, navController = navController)
        }
        composable(
            route = "complaint_form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            ComplaintFormScreen(complaintId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable(
            route = "complaint/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            ComplaintDetailScreen(complaintId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        composable("feedback_form") { FeedbackFormScreen(navController) }

        // ---------- Member 3 -- Module 6: Community Discussion & Emergency ----------
        composable("community_emergency") { CommunityEmergencyScreen(navController) }
        composable("new_post") { NewPostScreen(navController) }
        composable(
            route = "post/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            PostDetailScreen(postId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
        // resident's own chat with management (Home "Chat" icon)
        composable("resident_chat") {
            UserSession.currentUserId?.let { ChatThreadScreen(residentUserId = it, navController = navController) }
        }
        composable(
            route = "chat/{residentUserId}",
            arguments = listOf(navArgument("residentUserId") { type = NavType.IntType })
        ) { backStackEntry ->
            ChatThreadScreen(residentUserId = backStackEntry.arguments?.getInt("residentUserId") ?: -1, navController = navController)
        }
        composable("chat_inbox") { ChatInboxScreen(navController) }
        composable("post_reports") { PostReportsScreen(navController) }
        composable("emergency_sos") { EmergencySosScreen(navController) }
        composable("emergency_alerts") { EmergencyAlertsScreen(navController) }
        composable("emergency_contacts") { EmergencyContactsScreen(navController) }
        composable(
            route = "emergency_contact_form?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            EmergencyContactFormScreen(contactId = backStackEntry.arguments?.getInt("id") ?: -1, navController = navController)
        }
    }
}
