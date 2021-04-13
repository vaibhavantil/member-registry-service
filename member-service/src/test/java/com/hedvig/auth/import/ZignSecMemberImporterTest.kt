package com.hedvig.auth.import

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hedvig.auth.model.UserRepository
import com.hedvig.auth.model.ZignSecCredentialRepository
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
internal class ZignSecMemberImporterTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val zignSecCredentialRepository: ZignSecCredentialRepository
) {

    private val importer = ZignSecMemberImporter(
        userRepository,
        zignSecCredentialRepository,
        ObjectMapper().registerKotlinModule()
    )

    @Test
    fun `Norwegian - signed member is exported on event`() {
        val memberId = 123L
        val personalNumber = "01129955131"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            NorwegianMemberSignedEvent(memberId, personalNumber, providerJson, null)
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.zignSecCredential?.idProviderName).isEqualTo(idProviderName)
        assertThat(user?.zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    @Test
    fun `Norwegian - exporting the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "01129955131"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            NorwegianMemberSignedEvent(memberId, personalNumber, providerJson, null)
        )
        importer.on(
            NorwegianMemberSignedEvent(memberId, personalNumber, providerJson, null)
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
            NorwegianMemberSignedEvent(memberId1, personalNumber, providerJson, null)
        )
        importer.on(
            NorwegianMemberSignedEvent(memberId2, personalNumber, providerJson, null)
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    @Test
    fun `Danish - signed member is exported on event`() {
        val memberId = 123L
        val personalNumber = "220550-6218"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null)
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.zignSecCredential?.idProviderName).isEqualTo(idProviderName)
        assertThat(user?.zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    @Test
    fun `Danish - exporting the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "220550-6218"
        val idProviderName = "test-provider"
        val idProviderPersonId = "person-id"
        val providerJson = zignSecNotificationJson(idProviderName, idProviderPersonId)

        importer.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null)
        )
        importer.on(
            DanishMemberSignedEvent(memberId, personalNumber, providerJson, null)
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
            DanishMemberSignedEvent(memberId1, personalNumber, providerJson, null)
        )
        importer.on(
            DanishMemberSignedEvent(memberId2, personalNumber, providerJson, null)
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].zignSecCredential?.idProviderPersonId).isEqualTo(idProviderPersonId)
    }

    // This is given from ZignSec
    private fun zignSecNotificationJson(idProviderName: String, idProviderPersonId: String) = """{
        "id": "${UUID.randomUUID()}",
        "errors": [],
        "identity": {
            "IdProviderName": "$idProviderName",
            "IdProviderPersonId": "$idProviderPersonId"
        }
    }""".trimIndent()
}
