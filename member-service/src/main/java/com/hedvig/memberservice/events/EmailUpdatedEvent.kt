package com.hedvig.memberservice.events

class EmailUpdatedEvent(
    val id: Long,
    val email: String
): Traceable {
    override val memberId: Long
        get() = id

    override fun getValues() = mapOf("Email" to email)
}
