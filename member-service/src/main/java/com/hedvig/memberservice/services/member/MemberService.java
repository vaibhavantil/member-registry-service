package com.hedvig.memberservice.services.member;

import com.hedvig.external.bankID.bankIdTypes.CollectResponse;
import com.hedvig.memberservice.commands.BankIdSignCommand;
import com.hedvig.memberservice.commands.NorwegianSignCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MemberService {

  final private CommandGateway commandGateway;

  public MemberService(CommandGateway commandGateway) {
    this.commandGateway = commandGateway;
  }

  public void bankIdSignComplete(final long memberId, @NonNull final CollectResponse collectResponse) {
    this.commandGateway.sendAndWait(
        new
            BankIdSignCommand(
            memberId, collectResponse.getOrderRef(), collectResponse.getCompletionData().getSignature(), collectResponse.getCompletionData().getOcspResponse(),
            collectResponse.getCompletionData().getUser().getPersonalNumber()));
  }

  public void norwegianBankIdSignComplete(final long memberId, @NonNull final UUID referenceId, @NonNull final String peronalNumber, @NonNull final String providerJsonResponse) {
    this.commandGateway.sendAndWait(
      new
        NorwegianSignCommand(memberId, referenceId, peronalNumber, providerJsonResponse)
    );
  }
}
