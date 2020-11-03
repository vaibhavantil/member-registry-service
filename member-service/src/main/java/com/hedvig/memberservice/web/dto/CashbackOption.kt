package com.hedvig.memberservice.web.dto

import lombok.Value
import lombok.experimental.Wither
import java.util.*

/*        {
"id": "someid",
"title": "Rädda Barnen",
"description": "Lorem ipsum dolor sit amet...",
"selected": false,
"charity": true,
"imageUrl": "https://unsplash.it/400/200"
},
{
"id": "someotherid",
"title": "Mitt konto",
"description": "Lorem ipsum dolor sit amet...",
"selected": true,
"charity": false,
"imageUrl": "https://unsplash.it/400/200"
}
]*/
data class CashbackOption(
    @JvmField
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

data class NonLocalizedCashbackOption(
    val id: UUID,
    val nameKey: String,
    val titleKey: String,
    val descriptionKey: String,
    val selected: Boolean,
    val charity: Boolean,
    val imageUrl: String,
    val selectedUrl: String,
    val signatureKey: String,
    val paragraphKey: String
)
