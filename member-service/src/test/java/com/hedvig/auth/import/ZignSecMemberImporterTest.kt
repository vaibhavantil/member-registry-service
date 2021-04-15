package com.hedvig.auth.import

import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext

@DataJpaTest
internal class ZignSecMemberImporterTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val entityManager: TestEntityManager
) {

    private lateinit var importer: ZignSecMemberImporter

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        importer = context.autowireCapableBeanFactory.createBean(ZignSecMemberImporter::class.java)
    }

    @Test
    fun `Norwegian - signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "01129955131"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            NorwegianMemberSignedEvent(memberId, personalNumber, providerJson, null),
            Instant.now()
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.zignSecCredential?.idProviderName).isEqualTo(idProviderName)
        assertThat(user?.zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    @Test
    fun `Norwegian - imported member receives correct timestamp`() {
        val memberId = 123L
        val personalNumber = "201212121212"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)
        val time = Instant.now().minusSeconds(1000)

        importer.on(
            NorwegianMemberSignedEvent(memberId, personalNumber, providerJson, null),
            time
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user?.createdAt).isEqualTo(time)
        assertThat(user?.zignSecCredential?.createdAt).isEqualTo(time)
    }

    @Test
    fun `Norwegian - importing the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "01129955131"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            NorwegianMemberSignedEvent(memberId, personalNumber, providerJson, null),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            NorwegianMemberSignedEvent(memberId, personalNumber, providerJson, null),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
    }

    @Test
    fun `Norwegian - member signing with the same zignsec ids again should replace the corresponding user`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "01129955131"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            NorwegianMemberSignedEvent(memberId1, personalNumber, providerJson, null),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            NorwegianMemberSignedEvent(memberId2, personalNumber, providerJson, null),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    @Test
    fun `Danish - signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "220550-6218"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null),
            Instant.now()
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.zignSecCredential?.idProviderName).isEqualTo(idProviderName)
        assertThat(user?.zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    @Test
    fun `Danish - imported member receives correct timestamp`() {
        val memberId = 123L
        val personalNumber = "201212121212"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)
        val time = Instant.now().minusSeconds(1000)

        importer.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null),
            time
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user?.createdAt).isEqualTo(time)
        assertThat(user?.zignSecCredential?.createdAt).isEqualTo(time)
    }

    @Test
    fun `Danish - importing the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "220550-6218"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
    }

    @Test
    fun `Danish - member signing with the same zignsec ids again should replace the corresponding user`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "01129955131"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            DanishMemberSignedEvent(memberId1, personalNumber, providerJson, null),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            DanishMemberSignedEvent(memberId2, personalNumber, providerJson, null),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    // This is given from ZignSec
    private fun zignSecNotificationJson(idProviderName: String, idProviderPersonId: String) =
        """
            {
              "id": "a42a8afe-4071-4e99-8f9f-757c5942e1e5",
              "errors": [],
              "identity": {
                "CountryCode": "NO",
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
