package com.hedvig.memberservice.web.dto

import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import com.hedvig.memberservice.events.SSNUpdatedEvent

enum class Nationality {
    SWEDEN, NORWAY, DENMARK;

    companion object {
        fun toSSNUpdatedEventNationality(nationality: Nationality) = when (nationality) {
            SWEDEN -> SSNUpdatedEvent.Nationality.SWEDEN
            NORWAY -> SSNUpdatedEvent.Nationality.NORWAY
            DENMARK -> SSNUpdatedEvent.Nationality.DENMARK
        }

        fun toMemberSimpleSignedEventNationality(nationality: Nationality) = when (nationality) {
            SWEDEN -> MemberSimpleSignedEvent.Nationality.SWEDEN
            NORWAY -> MemberSimpleSignedEvent.Nationality.NORWAY
            DENMARK -> MemberSimpleSignedEvent.Nationality.DENMARK
        }
    }
}
