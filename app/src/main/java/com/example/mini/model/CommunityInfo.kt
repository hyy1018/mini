// #member2
// Rules, FAQ, facility guidelines and contact info residents can search (report 3NF: CommunityInfo).
package com.example.mini.model

data class CommunityInfo(
    val infoId: Int,
    val createdBy: Int,
    val infoType: String,
    val title: String,
    val content: String
)
