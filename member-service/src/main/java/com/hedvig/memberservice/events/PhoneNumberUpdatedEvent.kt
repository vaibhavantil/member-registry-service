package com.hedvig.memberservice.events

class PhoneNumberUpdatedEvent(
    val id: Long,
    val phoneNumber: String
) : Traceable {
    override val memberId: Long
        get() = id

    override fun getValues(): Map<String, Any> = mapOf("Phone number" to phoneNumber)
}
