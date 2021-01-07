package com.hedvig.memberservice.events

interface Traceable {
    val memberId: Long
    fun getValues(): Map<String, Any?>
}
