package com.hedvig.memberservice.query;

import com.hedvig.memberservice.events.SSNUpdatedEvent;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


import java.time.LocalDate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MemberEventListenerTest {

    @Mock
    MemberRepository memberRepository;

    MemberEventListener eventListener;

    @Before
    public void setUp() {
        eventListener = new MemberEventListener(memberRepository, null);
    }

    @Test
    public void givenSSNUpdatedEvent_SetBirthDate() {
        MemberEntity me = new MemberEntity();
        when(memberRepository.findOne(12l)).thenReturn(me);

        eventListener.on(new SSNUpdatedEvent(12l, "199510010101"));

        Assertions.assertThat(me.getBirthDate()).isEqualByComparingTo(LocalDate.of(1995, 10, 01));
    }
}