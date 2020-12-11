package com.hedvig.memberservice.web;

import com.google.common.collect.Lists;
import com.hedvig.integration.productsPricing.CampaignService;
import com.hedvig.memberservice.commands.UpdateEmailCommand;
import com.hedvig.memberservice.commands.UpdatePhoneNumberCommand;
import com.hedvig.memberservice.commands.UpdatePickedLocaleCommand;
import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.query.MemberRepository;
import com.hedvig.memberservice.query.TrackingIdEntity;
import com.hedvig.memberservice.query.TrackingIdRepository;
import com.hedvig.memberservice.services.cashback.CashbackService;
import com.hedvig.memberservice.services.cashback.CashbackServiceImpl;
import com.hedvig.memberservice.web.dto.*;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

@RestController()
@RequestMapping("/member/")
public class MembersController {

  private final MemberRepository repo;
  private final TrackingIdRepository trackingRepo;
  private final CommandGateway commandGateway;
  private final CampaignService campaignService;
  private final CashbackService cashbackService;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Value("${hedvig.counterkey:123}")
  public String counterKey;

  @Autowired
  public MembersController(
      MemberRepository repo,
      CommandGateway commandGateway,
      CampaignService campaignService,
      CashbackService cashbackService,
      TrackingIdRepository trackingRepo
  ) {
    this.repo = repo;
    this.commandGateway = commandGateway;
    this.campaignService = campaignService;
    this.cashbackService = cashbackService;
    this.trackingRepo = trackingRepo;
  }

  @RequestMapping(path = "/counter/321432", method = RequestMethod.GET)
  public ResponseEntity<CounterDTO> getCount(@RequestParam String key) {

    CounterDTO count = new CounterDTO();
    count.number = 123l;
    if (key.equals(counterKey)) {
      count.number = repo.countSignedMembers() + 100000l; // Hack to solve the broken Smiirl counter
    }
    return ResponseEntity.ok(count);
  }

  @GetMapping("/{memberId}")
  public ResponseEntity<Member> index(@PathVariable Long memberId) {

    Optional<MemberEntity> member = repo.findById(memberId);
    if (member.isPresent()) {

      return ResponseEntity.ok(new Member(member.get()));
    }

    return ResponseEntity.notFound().build();
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(@RequestHeader(value = "hedvig.token") Long hid) {
    Optional<MemberEntity> m = repo.findById(hid);

    MemberEntity me = m.orElseGet(() -> {
      MemberEntity m2 = new MemberEntity();
      m2.setFirstName("");
      m2.setLastName("");
      m2.setBirthDate(LocalDate.now());
      m2.setStreet("");
      m2.setCity("");
      m2.setApartment("");
      m2.setStatus(null);
      m2.setSsn("");
      m2.setEmail("");
      m2.setCashbackId(CashbackServiceImpl.Companion.getDEFAULT_SWEDISH_CASHBACK_OPTION().toString());
      return m2;
    });

    CashbackOption cashbackOption = null;

    if (me.getCashbackId() != null) {
      cashbackOption = cashbackService.getMembersCashbackOption(me.id)
        .orElseGet(() -> cashbackService.getDefaultCashback(me.id));
    }

    Optional<TrackingIdEntity> tId = trackingRepo.findByMemberId(hid);

    MemberMeDTO p = new MemberMeDTO(
      me.getId().toString(),
      me.getSsn(),
      String.format("%s %s", me.getFirstName(), me.getLastName()), me.getFirstName(),
      me.getLastName(),
      new ArrayList<>(),
      me.getBirthDate() == null ? null : me.getBirthDate().until(LocalDate.now()).getYears(),
      me.getEmail(),
      me.getStreet(),
      0,
      "",
      cashbackOption == null ? null : cashbackOption.name,
      "",
      LocalDate.MAX,
      cashbackOption == null ? null : cashbackOption.signature,
      cashbackOption == null ? null : String.format(cashbackOption.paragraph, me.getFirstName()),
      cashbackOption == null ? null : cashbackOption.selectedUrl,
      Lists.newArrayList(),
      tId.map(TrackingIdEntity::getTrackingId).orElse(null),
      me.getPhoneNumber());

    return ResponseEntity.ok(p);
  }

  @PostMapping("/email")
  public ResponseEntity<?> postEmail(@RequestHeader(value = "hedvig.token") Long hid, @RequestBody @Valid PostEmailRequestDTO body) {

    commandGateway.sendAndWait(new UpdateEmailCommand(hid, body.getEmail()));

    return ResponseEntity.accepted().build();
  }

  @PostMapping("/phonenumber")
  public ResponseEntity<?> postPhoneNumber(@RequestHeader(value = "hedvig.token") Long hid, @RequestBody @Valid PostPhoneNumberRequestDTO body) {

    commandGateway.sendAndWait(new UpdatePhoneNumberCommand(hid, body.getPhoneNumber()));

    return ResponseEntity.accepted().build();
  }

  @PostMapping("/language/update")
  public ResponseEntity<?> postLanguage(@RequestHeader(value = "hedvig.token") Long hid, @RequestBody @Valid PostLanguageRequestDTO body) {

    MemberControllerKotlinHelper memberControllerKotlinHelper = new MemberControllerKotlinHelper(commandGateway);

    return memberControllerKotlinHelper.postLanguage(hid, body);
  }

  @PostMapping("/pickedLocale/update")
  public ResponseEntity<Member> postPickedLocale(@RequestHeader(value = "hedvig.token") Long hid, @RequestBody @Valid PostPickedLocaleRequestDTO body) {

    commandGateway.sendAndWait(new UpdatePickedLocaleCommand(hid, body.getPickedLocale()));

    Optional<MemberEntity> member = repo.findById(hid);
    if (member.isPresent()) {

      return ResponseEntity.ok(new Member(member.get()));
    }

    return ResponseEntity.notFound().build();

  }
}
