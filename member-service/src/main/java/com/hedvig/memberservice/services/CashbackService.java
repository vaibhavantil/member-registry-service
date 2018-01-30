package com.hedvig.memberservice.services;

import com.hedvig.memberservice.web.dto.CashbackOption;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Supplier;

@Component
public class CashbackService {
    Map<UUID, CashbackOption> options = new HashMap<>();

    public CashbackService() {

        CashbackOption option3 = new CashbackOption(
                UUID.fromString("97b2d1d8-af4a-11e7-9b2b-bbc138162bb2"),
                "SOS Barnbyar",
                "Ge utsatta barn en trygg uppväxt.",
                true,
                true,
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos.png",
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/sos.png",
                "Lucas Carlsén, VD Hedvig AB",
                "\"Tack %s! Ditt stöd gör stor skillnad\""
        );

        CashbackOption option2 = new CashbackOption(
                UUID.fromString("11143ee0-af4b-11e7-a359-4f8b8d55e69f"),
                "Cancerfonden",
                "Hjälp till att besegra cancer.",
                false,
                true,
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/cancerfonden.png",
                "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/cancerfonden.png",
                "John Ardelius, CTO Hedvig AB",
                "\"Tack %s! Jag tycker att Cancerfonden gör ett bra arbete!\""
        );

        options.put(option3.id, option3);
        options.put(option2.id, option2);
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
