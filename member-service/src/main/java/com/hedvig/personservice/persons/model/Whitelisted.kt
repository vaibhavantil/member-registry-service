package com.hedvig.personservice.persons.model

import java.time.Instant
import javax.persistence.Embeddable

@Embeddable
data class Whitelisted(
    val whitelistedAt: Instant,
    val whitelistedBy: String
)
