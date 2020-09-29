package com.hedvig.memberservice.services.member;

import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod;
import com.hedvig.external.bankID.bankIdTypes.CollectResponse;
import com.hedvig.memberservice.commands.BankIdSignCommand;
import com.hedvig.memberservice.commands.ZignSecSignCommand;
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

  //todo: Don't use ZignSecAuthenticationMethod from zign sec module, istead remapp it
  public void signComplete(final long memberId, @NonNull final UUID referenceId, @NonNull final String peronalNumber, @NonNull final String providerJsonResponse, @NonNull final ZignSecAuthenticationMethod authenticationMethod) {
    this.commandGateway.sendAndWait(
      new
              ZignSecSignCommand(memberId, referenceId, peronalNumber, providerJsonResponse, authenticationMethod)
    );
  }
}
