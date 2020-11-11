package com.hedvig.integration.contentservice.dto

import com.hedvig.memberservice.web.dto.CashbackOption
import java.util.*

data class CashbackOptionDTO(
    val id: UUID,
    val name: String,
    val title: String,
    val description: String,
    val charity: Boolean,
    val imageUrl: String,
    val selectedUrl: String,
    val paragraph: String
) {
    fun toCashbackOption(selected: Boolean) = CashbackOption(
        id = id,
        name = name,
        title = title,
        description = description,
        selected = selected,
        charity = charity,
        imageUrl = imageUrl,
        selectedUrl = selectedUrl,
        signature = "",
        paragraph = paragraph
    )
}
