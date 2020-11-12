package com.hedvig.memberservice.services;

import com.hedvig.memberservice.web.dto.CashbackOption;
import org.junit.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertFalse;

public class CashbackServiceTest {


    @Test
    public void getCashbackOption_RETURNS_Empty_WHEN_CashbackIdNotFound() throws Exception {
        CashbackService service = new CashbackService();

        final Optional<CashbackOption> acctual = service.getCashbackOption(UUID.randomUUID());

        assertFalse(acctual.isPresent());
    }

}