// #member1
// One residential unit (report 3NF: Household). Every resident of the unit points to it.
// unitNo is always built by the app as "<block letter>-<floor>-<unit>", e.g. "A-10-05",
// so two residents who pick the same unit always land in the same household.
package com.example.mini.model

data class Household(
    val householdId: Int,
    val unitNo: String,
    val blockName: String,
    val householdStatus: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)
