package com.hedvig.memberservice.events;


import com.hedvig.memberservice.aggregates.BisnodeAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class LivingAddressUpdatedEventTest {

    @Test
    public void testfloorCalculation(){

        BisnodeAddress address = new BisnodeAddress(
                "Visiting",
                null,
                "Storgatan",
                "8",
                null,
                "1304",
                null,
                null,
                "12345",
                "Stockholm",
                "SE",
                new ArrayList<>());


        LivingAddressUpdatedEvent event = new LivingAddressUpdatedEvent(1234l, address);

        assertThat(event.getFloor()).isEqualTo(3);

    }

}