package com.hedvig.memberservice.events

class NameUpdatedEvent(
    override val memberId: Long,
    val firstName: String,
    val lastName: String
) : Traceable {
    override fun getValues() = mapOf(
        "First name" to firstName,
        "Last name" to lastName
    )
}
