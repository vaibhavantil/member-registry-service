package com.hedvig.memberservice.identity

import com.hedvig.memberservice.events.MemberIdentifiedEvent
import com.hedvig.memberservice.identity.models.MemberIdentityRevisionRepository
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("MemberIdentifiedEventListener")
class MemberIdentifiedEventListener {

    private lateinit var repository: MemberIdentityRevisionRepository

    @EventHandler
    fun on(event: MemberIdentifiedEvent) {

    }
}


