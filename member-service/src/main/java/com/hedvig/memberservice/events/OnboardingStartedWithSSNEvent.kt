package com.hedvig.memberservice.events

import org.axonframework.serialization.Revision
import org.axonframework.serialization.upcasting.event.EventUpcaster

@Deprecated("This event is deprecated don't use it for any new stuff")
@Revision("1.0")
class OnboardingStartedWithSSNEvent(
    memberId: Long,
    ssn: String,
    nationality: Nationality
) : SSNUpdatedEvent(memberId, ssn, nationality) {

    companion object {
        val upcasters: List<EventUpcaster> = listOf(
            NationalityFromSsnUpcaster("com.hedvig.memberservice.events.OnboardingStartedWithSSNEvent")
        )
    }
}
