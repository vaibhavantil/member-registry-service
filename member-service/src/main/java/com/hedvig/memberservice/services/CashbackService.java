package com.hedvig.memberservice.services;

import com.hedvig.memberservice.aggregates.PickedLocale;
import com.hedvig.memberservice.web.dto.CashbackOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class CashbackService {
  Map<UUID, CashbackOption> swedishOptions = new HashMap<>();
  Map<UUID, CashbackOption> norwegianOptions = new HashMap<>();

  public CashbackService() {

    CashbackOption option3 =
      new CashbackOption(
        UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2"),
        "SOS Barnbyar",
        "Ge fler barn en trygg uppväxt",
        "SOS Barnbyar gör ett fantastiskt arbete för att hjälpa barn som förlorat allt. Hos SOS Barnbyar får barnen en familj och en uppväxt i ett tryggt hem och möjlighet att gå i skolan med ambitionen att ta sig ur fattigdomen.",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png",
        "",
        "När Hedvig har betalat årets alla skador går din andel av överskottet till att ge fler barn en trygg uppväxt");

    CashbackOption option2 =
      new CashbackOption(
        UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f"),
        "Barncancerfonden",
        "Var med i kampen mot barncancer",
        "Barncancerfonden arbetar för att bekämpa barncancer och se till att drabbade och deras familjer får den vård och stöd de behöver. Pengarna går till forskning och stöd till de cirka 300 familjer som varje år drabbas av ett cancerbesked.",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/barncancerfonden-2.png",
        "",
        "När Hedvig har betalat årets alla skador går din andel av överskottet till att stödja kampen mot barncancer");

    swedishOptions.put(option3.id, option3);
    swedishOptions.put(option2.id, option2);

    CashbackOption norwayOption =
      new CashbackOption(
        UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003"),
        "SOS Barnbyar",
        "Gi flere barn en trygg oppvekst",
        "SOS-barnebyer ser en fantastisk jobb med å hjelpe barn som har mistet alt. I SOS-barnebyer får barna en familie og oppvekst i et trygt hjem og muligheten til å gå på skole med ambisjoner om å komme seg ut av fattigdom.",
        false,
        true,
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png", //todo change
        "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-barnbyar-logo.png", //todo change
        "",
        "Når Hedvig har betalt alle skader for året, går din del av overskuddet til å gi flere barn en trygg oppvekst");

    norwegianOptions.put(norwayOption.id, norwayOption);

  }

  public Optional<CashbackOption> getCashbackOption(UUID cashbackId) {
    try {
      CashbackOption cashbackOption = norwegianOptions.get(cashbackId);
      if (cashbackOption != null)
        return Optional.ofNullable(cashbackOption);
    } catch (NullPointerException ex) {
    }

    return Optional.ofNullable(swedishOptions.get(cashbackId));
  }

  public List<CashbackOption> getOptions(PickedLocale pickedLocale) {
    if (isNorwegian(pickedLocale)) {
      return new ArrayList<>(norwegianOptions.values());
    }
    return new ArrayList<>(swedishOptions.values());
  }

  public UUID getDefaultId(PickedLocale pickedLocale) {
    if (isNorwegian(pickedLocale))
      return UUID.fromString("02c99ad8-75aa-11ea-bc55-0242ac130003");

    return UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2");
  }

  public CashbackOption getDefaultCashback(PickedLocale pickedLocale) {
    return swedishOptions.get(getDefaultId(pickedLocale));
  }

  private Boolean isNorwegian(PickedLocale pickedLocale) {
    return pickedLocale == PickedLocale.nb_NO || pickedLocale == PickedLocale.en_NO;
  }
}
