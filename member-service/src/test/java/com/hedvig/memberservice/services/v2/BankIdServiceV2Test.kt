package com.hedvig.memberservice.services.v2

import com.hedvig.auth.services.UserService
import com.hedvig.external.bankID.bankId.BankIdApi
import com.hedvig.external.bankID.bankIdTypes.*
import com.hedvig.external.bankID.bankIdTypes.Collect.*
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.jobs.SwedishBankIdMetrics
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.quartz.Scheduler
import org.mockito.Mockito.`when` as whenever

@RunWith(MockitoJUnitRunner::class)
class BankIdServiceV2Test {

    @Mock lateinit var bankIdApi: BankIdApi
    @Mock lateinit var commandGateway: CommandGateway
    @Mock lateinit var redisEventPublisher: RedisEventPublisher
    @Mock lateinit var scheduler: Scheduler
    @Mock lateinit var collectRepository: CollectRepository
    @Mock lateinit var apiGatewayService: ApiGatewayService
    @Mock lateinit var swedishBankIdMetrics: SwedishBankIdMetrics
    @Mock lateinit var userService: UserService

    lateinit var bankIdServiceV2: BankIdServiceV2

    @Before
    fun setup() {
        bankIdServiceV2 = BankIdServiceV2(
            bankIdApi = bankIdApi,
            commandGateway = commandGateway,
            redisEventPublisher = redisEventPublisher,
            scheduler = scheduler,
            collectRepository = collectRepository,
            apiGatewayService = apiGatewayService,
            swedishBankIdMetrics = swedishBankIdMetrics,
            userService = userService
        )
    }

    @Test
    fun authCollect_withStatusComplete_shouldUpdateAuthSessionWithSuccess() {
        whenever(bankIdApi.collect(CollectRequest("xyz")))
            .thenReturn(CollectResponse(
                "xyz",
                CollectStatus.complete,
                "",
                CompletionData(
                    User(
                        "190001010101",
                        "Testy Tester",
                        "Testy",
                        "Tester"
                    ),
                    Device("0.0.0.0"),
                    Cert(0,0),
                    "",
                    ""
                )
            ))

        val user =
            com.hedvig.auth.models.User(
                associatedMemberId = "54321"
            )

        whenever(userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(
                personalNumber = "190001010101"
            ), UserService.Context(
                onboardingMemberId = "12345"
            ))).thenReturn(user)

        bankIdServiceV2.authCollect(referenceToken = "xyz", memberId = 12345)

        Mockito.verify(commandGateway).sendAndWait<Any>(InactivateMemberCommand(12345))
        Mockito.verify(apiGatewayService).reassignMember(12345, 54321)
        Mockito.verify(redisEventPublisher).onAuthSessionUpdated(12345, AuthSessionUpdatedEventStatus.SUCCESS)
    }

}
