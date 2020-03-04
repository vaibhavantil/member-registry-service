package com.hedvig.memberservice.web.dto

data class PostMarketRequestDTO (
     val market : Market
)

enum class Market {
    SE,
    NO
}

