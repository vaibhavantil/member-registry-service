package com.hedvig.memberservice.events


class MemberAuthenticatedEvent(
    override val memberId: Long,
    val bankIdReferenceToken: String
) : Traceable {

    override fun getValues(): Map<String, Any> = mapOf(
        "Member authenticated" to ""
    )
}
