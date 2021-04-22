package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(UserService::class)
internal class DanishMemberSignedEventHandlerTest @Autowired constructor(
    private val userService: UserService,
    private val entityManager: TestEntityManager
) {

    private lateinit var eventHandler: DanishMemberSignedEventHandler

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        eventHandler = context.autowireCapableBeanFactory.createBean(DanishMemberSignedEventHandler::class.java)
    }

    @Test
    fun `signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "220550-6218"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        eventHandler.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null)
        )

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = idProviderName,
                idProviderPersonId = idProviderPersonId
            ),
            UserService.Context(onboardingMemberId = null)
        )
        assertThat(user).isNotNull
        assertThat(user?.associatedMemberId).isEqualTo(memberId.toString())
    }

    @Test
    fun `importing the same user twice simply ignores second attempt`() {
        val memberId = 123L
        val personalNumber = "220550-6218"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        eventHandler.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null)
        )
        entityManager.clear()
        eventHandler.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null)
        )
    }

    // This is given from ZignSec
    private fun zignSecNotificationJson(idProviderName: String, idProviderPersonId: String) =
        """
            {
              "id": "a42a8afe-4071-4e99-8f9f-757c5942e1e5",
              "errors": [],
              "identity": {
                "CountryCode": "DK",
                "FirstName": "first",
                "LastName": "last",
                "FullName": "first last",
                "DateOfBirth": "2012-12-12",
                "Age": 8,
                "Gender": "",
                "IdProviderName": "$idProviderName",
                "IdentificationDate": "2020-02-11T15:45:23Z",
                "IdProviderRequestId": "",
                "IdProviderPersonId": "$idProviderPersonId",
                "CustomerPersonId": ""
              },
              "BANKIdNO_OIDC": "{\r\n  \"access_token\": \"access_token\",\r\n  \"expires_in\": 300,\r\n  \"refresh_expires_in\": 1800,\r\n  \"refresh_token\": \"access_token\",\r\n  \"token_type\": \"bearer\",\r\n  \"id_token\": \"id_token\",\r\n  \"not-before-policy\": 0,\r\n  \"session_state\": \"session_state\",\r\n  \"scope\": \"openid nnin_altsub profile\"\r\n}",
              "method": "nbid_oidc"
            }
        """.trimIndent()
}
