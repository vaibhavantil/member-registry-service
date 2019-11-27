package com.hedvig.memberservice.personservice

import com.hedvig.memberservice.aggregates.FraudulentStatus
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.Flag
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PersonServiceTest {

    @Test
    fun fraudFlagTest() {
        val member1 = MemberEntity()
        member1.ssn = "9901019999"
        member1.fraudulentStatus = FraudulentStatus.NOT_FRAUD
        val member2 = MemberEntity()
        member2.ssn = "9901019999"
        member2.fraudulentStatus = FraudulentStatus.CONFIRMED_FRAUD
        val member3 = MemberEntity()
        member3.ssn = "9901019999"
        member3.fraudulentStatus = FraudulentStatus.SUSPECTED_FRAUD
        val member4 = MemberEntity()
        member4.ssn = "9901019999"
        member4.fraudulentStatus = FraudulentStatus.NOT_FRAUD
        val members = listOf(member1, member2, member3, member4)
        val fraudFlag = PersonService.calculateFraudFlag(members)
        assertThat(fraudFlag).isEqualTo(Flag.RED)
    }
}
