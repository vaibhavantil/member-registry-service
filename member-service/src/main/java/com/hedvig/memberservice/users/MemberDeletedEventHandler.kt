package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.MemberDeletedEvent
import org.axonframework.eventhandling.EventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MemberDeletedEventHandler {

    @Autowired
    private lateinit var userService: UserService

    @EventHandler
    fun on(e: MemberDeletedEvent) {
        userService.deleteUserWithAssociatedMemberId(e.memberId.toString())
    }
}
