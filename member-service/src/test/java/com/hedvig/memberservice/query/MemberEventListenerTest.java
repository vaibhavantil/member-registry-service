package com.hedvig.memberservice.query;

import static org.mockito.Mockito.when;

import com.hedvig.memberservice.events.SSNUpdatedEvent;
import java.time.LocalDate;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MemberEventListenerTest {

  @Mock
  MemberRepository memberRepository;

  @Mock
  TrackingIdRepository trackingIdRepository;

  @Mock
  TraceMemberRepository traceMemberRepository;

  private MemberEventListener eventListener;

    @Before
    public void setUp() {
        eventListener = new MemberEventListener(memberRepository, null, trackingIdRepository, traceMemberRepository);
    }

    @Test
    public void givenSSNUpdatedEvent_SetBirthDate() {
        MemberEntity me = new MemberEntity();
        when(memberRepository.findById(12l)).thenReturn(Optional.of(me));

        eventListener.on(new SSNUpdatedEvent(12l, "199510010101"));

        Assertions.assertThat(me.getBirthDate()).isEqualTo(LocalDate.of(1995, 10, 01));
    }
}
