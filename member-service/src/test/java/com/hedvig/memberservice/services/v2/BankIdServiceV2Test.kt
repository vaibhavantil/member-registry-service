package com.hedvig.memberservice.services.v2

import com.hedvig.auth.services.UserService
import com.hedvig.external.bankID.bankId.BankIdApi
import com.hedvig.external.bankID.bankIdTypes.Collect.Cert
import com.hedvig.external.bankID.bankIdTypes.Collect.Device
import com.hedvig.external.bankID.bankIdTypes.Collect.User
import com.hedvig.external.bankID.bankIdTypes.CollectRequest
import com.hedvig.external.bankID.bankIdTypes.CollectResponse
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.external.bankID.bankIdTypes.CompletionData
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.AuthenticatedIdentificationCommand
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.jobs.SwedishBankIdMetrics
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.Scheduler

class BankIdServiceV2Test {

    @MockK lateinit var bankIdApi: BankIdApi
    @MockK lateinit var commandGateway: CommandGateway
    @MockK lateinit var redisEventPublisher: RedisEventPublisher
    @MockK lateinit var scheduler: Scheduler
    @MockK lateinit var collectRepository: CollectRepository
    @MockK lateinit var apiGatewayService: ApiGatewayService
    @MockK lateinit var swedishBankIdMetrics: SwedishBankIdMetrics
    @MockK lateinit var userService: UserService

    lateinit var bankIdServiceV2: BankIdServiceV2

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
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
        every {
            bankIdApi.collect(CollectRequest("xyz"))
        } returns CollectResponse(
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
                Cert(0, 0),
                "",
                ""
            )
        )

        val user = com.hedvig.auth.models.User(
            associatedMemberId = "54321"
        )

        every {
            userService.findOrCreateUserWithCredentials(
                UserService.Credentials.SwedishBankID(
                    personalNumber = "190001010101"
                ), onboardingMemberId = "12345")
        } returns user

        bankIdServiceV2.authCollect(referenceToken = "xyz", memberId = 12345)

        verify {
            commandGateway.sendAndWait<Any>(InactivateMemberCommand(12345))
        }
        verify {
            apiGatewayService.reassignMember(12345, 54321)
        }
        verify {
            redisEventPublisher.onAuthSessionUpdated(12345, AuthSessionUpdatedEventStatus.SUCCESS)
        }
    }

    @Test
    fun authCollect_withStatusComplete_shouldFireAuthenticatedIdentificationCommand() {
        every {
            bankIdApi.collect(CollectRequest("xyz"))
        } returns CollectResponse(
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
                Cert(0, 0),
                "",
                ""
            )
        )

        val user = com.hedvig.auth.models.User(
            associatedMemberId = "54321"
        )

        every {
            userService.findOrCreateUserWithCredentials(
                UserService.Credentials.SwedishBankID(
                    personalNumber = "190001010101"
                ), onboardingMemberId = "12345")
        } returns user

        bankIdServiceV2.authCollect(referenceToken = "xyz", memberId = 12345)

        verify {
            commandGateway.sendAndWait<Any>(
                AuthenticatedIdentificationCommand(
                    54321L,
                    "Testy",
                    "Tester",
                    "190001010101",
                    "SE",
                    AuthenticatedIdentificationCommand.Source.SwedishBankID
                )
            )
        }
    }
}
