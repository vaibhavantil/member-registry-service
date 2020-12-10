package com.hedvig.memberservice.web.v2

import com.hedvig.integration.botService.BotService
import com.hedvig.memberservice.commands.CreateMemberCommand
import com.hedvig.memberservice.correction.CorrectSwedishSsnEventComponent
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.web.v2.dto.HelloHedvigResponse
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.retry.support.RetryTemplate
import org.springframework.web.bind.annotation.*
import java.security.SecureRandom
import java.util.*
import kotlin.math.abs

@RestController
@RequestMapping("/v2/member")
class MembersControllerV2 @Autowired constructor(
    val retryTemplate: RetryTemplate,
    val memberRepository: MemberRepository,
    val randomGenerator: SecureRandom = SecureRandom.getInstance("SHA1PRNG"),
    val commandGateway: CommandGateway,
    val botService: BotService,
    val correctSwedishSsnEventComponent: CorrectSwedishSsnEventComponent
) {
    @PostMapping("/helloHedvig", produces = ["application/json"])
    fun helloHedvig(
        @RequestBody(required = false) json: String?,
        @RequestHeader(value = "Accept-Language", required = false) acceptLanguage: String?
    ): ResponseEntity<HelloHedvigResponse> {
        val id = retryTemplate.execute<Long, Exception> {
            var memberId: Long?
            var member: Optional<MemberEntity>

            do {
                memberId = abs(this.randomGenerator.nextLong() % 1000000000)
                member = memberRepository.findById(memberId)
            } while (member.isPresent)

            commandGateway.send<Void>(CreateMemberCommand(memberId!!, acceptLanguage))
            return@execute memberId
        }

        botService.initBotService(id, json)

        log.info("New member created with id: " + id!!)
        return ResponseEntity.ok().body(HelloHedvigResponse(id))
    }

    //this will be removed once used
    @GetMapping("/correctSwedishSsnEvent")
    fun correction(): ResponseEntity<Int> {
        return ResponseEntity.ok().body(correctSwedishSsnEventComponent.addCorrectionEventsOnAllSwedishMembers())
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(MembersControllerV2::class.java)
    }
}
