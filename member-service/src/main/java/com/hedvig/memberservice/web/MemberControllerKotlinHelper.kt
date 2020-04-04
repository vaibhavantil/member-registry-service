package com.hedvig.memberservice.web

import com.hedvig.memberservice.commands.UpdateAcceptLanguageCommand
import com.hedvig.memberservice.web.dto.PostLanguageRequestDTO
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.server.ResponseStatusException

class MemberControllerKotlinHelper(private val commandGateway: CommandGateway) {
    fun postLanguage(hid: Long, body: PostLanguageRequestDTO): ResponseEntity<Any> {
        if (body.acceptLanguage == null && body.graphqlLocale == null) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Both acceptLanguage and graphqlLocale cannot be null"
            )
        }

        val graphqlLanguageTag = body.graphqlLocale?.toLocale()?.toLanguageTag()

        commandGateway.sendAndWait<Void>(
            UpdateAcceptLanguageCommand(
                hid,
                graphqlLanguageTag ?: (body.acceptLanguage?.replace('_', '-'))!!
            )
        )

        return ResponseEntity.accepted().build()
    }
}
