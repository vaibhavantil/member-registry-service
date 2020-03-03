package com.hedvig.memberservice.web.dto

data class PostLocaleRequestDTO (
     val market : Market
)

enum class Market {
    SE,
    NO
}

