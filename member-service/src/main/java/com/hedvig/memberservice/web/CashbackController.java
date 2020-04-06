package com.hedvig.memberservice.web;

import com.hedvig.memberservice.aggregates.PickedLocale;
import com.hedvig.memberservice.commands.SelectNewCashbackCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.services.CashbackService;
import com.hedvig.memberservice.web.dto.CashbackOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cashback")
public class CashbackController {

  final MemberRepository memberRepository;
  private final CashbackService cashbackService;
  CommandGateway commandGateway;

  @Autowired
  public CashbackController(
      CommandGateway commandGateway,
      MemberRepository memberRepository,
      CashbackService cashbackService) {
    this.commandGateway = commandGateway;
    this.memberRepository = memberRepository;
    this.cashbackService = cashbackService;
  }

  @GetMapping("options")
  public ResponseEntity<List<CashbackOption>> options(
      @RequestHeader(value = "hedvig.token") Long hid) {
    Optional<MemberEntity> member = memberRepository.findById(hid);

    if (member.isPresent()) {

      List<CashbackOption> cashbackOptions =
          cashbackService
              .getOptions(member.get().pickedLocale)
              .stream()
              .map(
                  o -> {
                    if (o.id.toString().equals(member.get().getCashbackId())) {
                      return o.withSelected(true);
                    }

                    return o;
                  })
              .collect(Collectors.toList());

      return ResponseEntity.ok(cashbackOptions);
    }

    return ResponseEntity.notFound().build();
  }

  @PostMapping("")
  public ResponseEntity<String> cashback(
      @RequestHeader(value = "hedvig.token") Long hid, @RequestParam UUID optionId) {

    Optional<CashbackOption> opt = cashbackService.getCashbackOption(optionId);
    Optional<MemberEntity> member = memberRepository.findById(hid);

    if (opt.isPresent() && member.isPresent()) {
      commandGateway.sendAndWait(new SelectNewCashbackCommand(hid, optionId));
      return ResponseEntity.noContent().build(); // ok().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
