package com.hedvig.memberservice;

import com.hedvig.external.bisnodeBCI.BisnodeClient;
import com.hedvig.memberservice.aggregates.MemberAggregate;
import com.hedvig.memberservice.aggregates.MemberStatus;
import com.hedvig.memberservice.commands.MemberUpdateContactInformationCommand;
import com.hedvig.memberservice.commands.SelectNewCashbackCommand;
import com.hedvig.memberservice.commands.StartOnboardingWithSSNCommand;
import com.hedvig.memberservice.events.*;
import com.hedvig.memberservice.web.dto.Address;
import com.hedvig.memberservice.web.dto.StartOnboardingWithSSNRequest;
import com.hedvig.memberservice.web.dto.UpdateContactInformationRequest;
import org.axonframework.eventsourcing.AbstractAggregateFactory;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
public class MemberAggregateTests {

	private FixtureConfiguration<MemberAggregate> fixture;

	@MockBean
	BisnodeClient bisnodeClient;

	private class AggregateFactoryM<T > extends AbstractAggregateFactory<T> {

		protected AggregateFactoryM(Class<T> aggregateType) {
			super(aggregateType);
		}

		@Override
		protected T doCreateAggregate(String aggregateIdentifier, DomainEventMessage firstEvent) {
			return (T) new MemberAggregate(bisnodeClient);
		}
	}

	@Before
	public void setUp() {
		fixture = new AggregateTestFixture<>(MemberAggregate.class);
		fixture.registerAggregateFactory(new AggregateFactoryM<>(MemberAggregate.class));
	}

	@Test
	public void contextLoads() {
		Long memberId = 123l;
		fixture.given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED));
	}

	@Test
	public void MemberUpdatePersonalInformation(){
		Long memberId = 1234l;

		UpdateContactInformationRequest request = new UpdateContactInformationRequest();
		request.setFirstName("Arn");
		request.setLastName("Magnusson");
		Address address = new Address();
		address.setStreet("Spånga bro");
		address.setCity("Spånga");
		address.setApartmentNo("1104");
		address.setZipCode("55748");
		request.setAddress(address);

		fixture.
				given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED)).
				when(new MemberUpdateContactInformationCommand(memberId, request)).
				expectSuccessfulHandlerExecution().expectEvents(
						new NameUpdatedEvent(memberId, request.getFirstName(), request.getLastName()),
						new LivingAddressUpdatedEvent(memberId, address.getStreet(), address.getCity(), address.getZipCode(), address.getApartmentNo())
		);
	}

	@Test
	public void StartOnBoardingFromSSN(){
		Long memberId = 1234l;

		String ssn = "192005059999";
		StartOnboardingWithSSNRequest request = new StartOnboardingWithSSNRequest(ssn);

		fixture.
				given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED)).
				when(new StartOnboardingWithSSNCommand(memberId, request)).
				expectSuccessfulHandlerExecution().expectEvents(
				new OnboardingStartedWithSSNEvent(memberId, ssn),
				new MemberStartedOnBoardingEvent(memberId, MemberStatus.ONBOARDING));
	}

	@Test
	public void SelectNewCashbackCommand() {
		Long memberId = 1234l;
		String cashbackId = "328354a4-d119-11e7-ac68-139bd471ea9a";

		fixture.given(new MemberCreatedEvent(memberId, MemberStatus.INITIATED)).
				when(new SelectNewCashbackCommand(memberId, UUID.fromString(cashbackId))).
				expectSuccessfulHandlerExecution().
				expectEvents(new NewCashbackSelectedEvent(memberId, cashbackId));

	}

}
