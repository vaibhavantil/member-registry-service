package com.hedvig.memberservice.services;

import com.hedvig.memberservice.web.dto.CashbackOption;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CashbackService {
    Map<UUID, CashbackOption> options = new HashMap<>();

    public CashbackService() {

        CashbackOption option3 = new CashbackOption(
                UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2"),
                "SOS Barnbyar",
                "Ge utsatta barn en trygg uppväxt",
                "SOS Barnbyar gör ett fantastiskt arbete för att hjälpa barn som förlorat allt. Hos SOS Barnbyar får barnen en familj och en uppväxt i ett tryggt hem och möjlighet att gå i skolan med ambitionen att ta sig ur fattigdomen.",
                false,
                true,
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos.png",
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos-profile.png",
                "",
                "När Hedvig har betalat årets alla skador går din andel av överskottet till att ge utsatta barn en trygg uppväxt"
        );

        // CashbackOption option2 = new CashbackOption(
        //         UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f"),
        //         "Cancerfonden",
        //         "Hjälp till att besegra cancer",
        //         "Cancerfonden arbetar för att besegra cancer. Målet är att fler ska överleva och färre drabbas. Organisationen arbetar också för att vården ska vara så bra som möjligt för dem som insjuknar och att deras närstående för stöd och hjälp. För att nå dit arbetar organisationen bland annat med forskningsfinansiering.",
        //         false,
        //         true,
        //         "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/cancerfonden.png",
        //         "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/cancerfonden-profile.png",
        //         "",
        //         "När Hedvig har betalat årets alla skador går din andel av överskottet till hjälpa till att besegra cancer"
        // );

        options.put(option3.id, option3);
        // options.put(option2.id, option2);
    }

    public Optional<CashbackOption> getCashbackOption(UUID cashbackId) {
        return Optional.of(options.get(cashbackId));
    }

    public List<CashbackOption> getOptions() {
        return new ArrayList<>(options.values());
    }

    public UUID getDefaultId() {
        return UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2");
    }

    public CashbackOption getDefaultCashback() {
        return options.get(getDefaultId());
    }
}
