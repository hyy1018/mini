// #member1
// The service icons on the Resident Home grid. "?tab=" opens a module hub on a specific tab.
package com.example.mini.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign

import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import com.example.mini.model.MainHubItem

object MainHubData {
    val services = listOf(
        // Module 2 (Member 1)
        MainHubItem("visitor", "Visitor", Icons.Filled.ConfirmationNumber, "visitor_security"),
        // Module 4 (Member 2)
        MainHubItem("booking", "Booking", Icons.Filled.EventAvailable, "facility_maintenance?tab=0"),
        MainHubItem("repair", "Repair", Icons.Filled.Build, "facility_maintenance?tab=2"),
        // Module 3 (Member 2)
        MainHubItem("news", "News", Icons.Filled.Campaign, "community_engagement?tab=0"),
        // Module 5 (Member 3) -- Feedback took Chat's old spot; Chat is now in the top bar
        MainHubItem("complaint", "Complaint", Icons.Filled.Feedback, "complaint_feedback?tab=0"),
        MainHubItem("feedback", "Feedback", Icons.Filled.ThumbUp, "complaint_feedback?tab=1"),
        // Module 6 (Member 3)
        MainHubItem("feed", "Feed", Icons.Filled.Forum, "community_emergency"),
        // Module 3 (Member 2)
        MainHubItem("faq", "FAQ", Icons.AutoMirrored.Filled.HelpOutline, "community_engagement?tab=2")
    )
}
