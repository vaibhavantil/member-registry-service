package com.hedvig.memberservice.web.dto

import java.util.UUID

data class CashbackOption(
    val id: UUID? = null,
    @JvmField
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    val selected: Boolean? = null,
    val charity: Boolean? = null,
    val imageUrl: String? = null,
    @JvmField
    val selectedUrl: String? = null,
    @JvmField
    val signature: String? = null,
    @JvmField
    val paragraph: String? = null
)
