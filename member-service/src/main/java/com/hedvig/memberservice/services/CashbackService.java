package com.hedvig.memberservice.services;

import com.hedvig.memberservice.web.dto.CashbackOption;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;

@Component
public class CashbackService {
    Map<UUID, CashbackOption> options = new HashMap<>();
    private CashbackOption defaultCashback;

    public CashbackService() {

        CashbackOption option1 = new CashbackOption(
                UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2"),
                "Rädda barnen",
                "Rädda Barnen kämpar för barns rättigheter i Sverige och i världen. Vi arbetar på lång- och kort sikt för att inget barn ska behöva fara illa.",
                false,
                true,
                "https://www.rawfoodmiddagar.com/images/webshopen/rc3a4dda-barnen-logga-sv.jpg",
                "https://www.rawfoodmiddagar.com/images/webshopen/rc3a4dda-barnen-logga-sv.jpg",
                "Elisabeth Dahlin, Generaldirektör",
                "\"Tack kära %s för att du bidrar till att hjälpa fler utsatta barn\""
        );

        CashbackOption option3 = new CashbackOption(
                UUID.fromString("d24c427e-d110-11e7-a47e-0b4e39412e98"),
                "Bancancerfonden",
                "Barncancerfonden arbetar för att bekämpa barncancer och se till att drabbade och deras familjer får den vård och stöd de behöver.",
                true,
                true,
                "http://ljus.barncancerfonden.se/images/barncancerfonden.png",
                "http://ljus.barncancerfonden.se/images/barncancerfonden.png",
                "Isabelle Ducellier, Generaldirektör",
                "\"Tack kära %s för att du bidrar till att rädda livet på fler cancerdrabbade barn\""
        );

        CashbackOption option2 = new CashbackOption(
                UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f"),
                "Stadsmissionen",
                "Stockholms Stadsmission är en idéburen organisation som arbetar för att skapa ett mänskligare samhälle för alla.",
                false,
                true,
                "https://www.stadsmissionen.se/profiles/ssm/themes/custom/ssmtheme/images/share_default.png",
                "https://www.stadsmissionen.se/profiles/ssm/themes/custom/ssmtheme/images/share_default.png",
                "Marika Markovits, Direktör",
                "\"Tack %s! Ditt stöd gör stor skillnad\""
        );

        options.put(option3.id, option3);
        options.put(option1.id, option1);
        options.put(option2.id, option2);
    }

    public Optional<CashbackOption> getCashbackOption(UUID cashbackId) {
        return Optional.of(options.get(cashbackId));
    }

    public List<CashbackOption> getOptions() {
        return new ArrayList<>(options.values());
    }

    public UUID getDefaultId() {
        return UUID.fromString("d24c427e-d110-11e7-a47e-0b4e39412e98");
    }

    public CashbackOption getDefaultCashback() {
        return options.get(getDefaultId());
    }
}
