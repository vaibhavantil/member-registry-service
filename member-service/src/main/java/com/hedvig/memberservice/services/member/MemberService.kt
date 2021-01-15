package com.hedvig.memberservice.services.member

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.bankID.bankIdTypes.CollectResponse
import com.hedvig.memberservice.commands.SwedishBankIdSignCommand
import com.hedvig.memberservice.commands.ZignSecSignCommand
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket.Companion.fromAuthenticationMethod
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.lang.NonNull
import org.springframework.lang.Nullable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class MemberService(private val commandGateway: CommandGateway) {
    fun bankIdSignComplete(memberId: Long, collectResponse: CollectResponse) {
        commandGateway.sendAndWait<Any>(
            SwedishBankIdSignCommand(
                memberId,
                collectResponse.orderRef,
                collectResponse.completionData.signature,
                collectResponse.completionData.ocspResponse,
                collectResponse.completionData.user.personalNumber
            ))
    }

    fun signComplete(
        memberId: Long,
        referenceId: UUID,
        personalNumber: String,
        providerJsonResponse: String,
        authenticationMethod: ZignSecAuthenticationMethod,
        firstName: String?,
        lastName: String?
    ) {
        commandGateway.sendAndWait<Any>(
            ZignSecSignCommand(
                memberId,
                referenceId,
                personalNumber,
                providerJsonResponse,
                fromAuthenticationMethod(authenticationMethod),
                firstName,
                lastName
            )
        )
    }
}
