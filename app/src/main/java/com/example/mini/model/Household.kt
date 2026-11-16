// #member1
// One residential unit (report 3NF: Household). Every resident of the unit points to it.
package com.example.mini.model

data class Household(
    val householdId: Int,
    val unitNo: String,
    val blockName: String,
    val address: String,
    val householdStatus: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)
