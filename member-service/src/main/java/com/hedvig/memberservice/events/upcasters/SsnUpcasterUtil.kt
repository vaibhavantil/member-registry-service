package com.hedvig.memberservice.events.upcasters

import com.hedvig.memberservice.events.SSNUpdatedEvent
import java.lang.RuntimeException

fun String?.nationalityFromSsn() = when (this?.length) {
    10 -> SSNUpdatedEvent.Nationality.DENMARK
    11 -> SSNUpdatedEvent.Nationality.NORWAY
    12 -> SSNUpdatedEvent.Nationality.SWEDEN
    null -> SSNUpdatedEvent.Nationality.SWEDEN
    else -> throw RuntimeException("SsnUpcasterUtil failed ssn: $this")
}
