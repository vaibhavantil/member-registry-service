package com.hedvig.memberservice.events

data class MemberSignedEvent(
    val id: Long,
    val referenceId: String,
    val signature: String,
    val oscpResponse: String,
    /**
     * [ssn] is null for the oldest events in the system, pre October 2018
     */
    val ssn: String?
) {
    @Deprecated(
        """This field exists on some of the events in the database, these events should be cleand up.
    if you need to get the signedOnDate get the eventTimeStamp instead"""
    )
    val signedOn: String? = null
}
