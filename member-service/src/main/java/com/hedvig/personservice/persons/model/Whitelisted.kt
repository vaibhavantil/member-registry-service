package com.hedvig.personservice.persons.model

import java.time.Instant

data class Whitelisted(
    val whitelistedAt: Instant,
    val whitelistedBy: String
)
