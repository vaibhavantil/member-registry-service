package com.hedvig.memberservice.repositories

import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner

@DataJpaTest
@RunWith(SpringRunner::class)
class MemberRepositoryTests {
    @Autowired
    lateinit var memberRepository: MemberRepository

    @Test
    fun findNonSignedMembersBySsnOrEmailAndNotId() {
        val memberThatSigns = MemberEntity()
        memberThatSigns.id = 123L
        memberThatSigns.email = "signed@email.com"
        memberThatSigns.ssn = "200001010000"
        memberThatSigns.status = MemberStatus.ONBOARDING

        val nonSignedMemberWithSameEmailAndSsn = MemberEntity()
        nonSignedMemberWithSameEmailAndSsn.id = 234L
        nonSignedMemberWithSameEmailAndSsn.email = "signed@email.com"
        nonSignedMemberWithSameEmailAndSsn.ssn = "200001010000"
        nonSignedMemberWithSameEmailAndSsn.status = MemberStatus.ONBOARDING

        val nonSignedMemberWithSameEmail = MemberEntity()
        nonSignedMemberWithSameEmail.id = 345L
        nonSignedMemberWithSameEmail.email = "signed@email.com"
        nonSignedMemberWithSameEmail.status = MemberStatus.ONBOARDING

        val nonSignedMemberWithSameSsn = MemberEntity()
        nonSignedMemberWithSameSsn.id = 456L
        nonSignedMemberWithSameSsn.email = "other@email.com"
        nonSignedMemberWithSameSsn.ssn = "200001010000"
        nonSignedMemberWithSameSsn.status = MemberStatus.ONBOARDING

        val otherMemberCompletely = MemberEntity()
        otherMemberCompletely.id = 567L
        otherMemberCompletely.email = "other@email.com"
        otherMemberCompletely.ssn = "199912319999"
        otherMemberCompletely.status = MemberStatus.ONBOARDING

        val memberWithSameSsnAndSignedSomehow = MemberEntity()
        memberWithSameSsnAndSignedSomehow.id = 678L
        memberWithSameSsnAndSignedSomehow.email = "signed@email.com"
        memberWithSameSsnAndSignedSomehow.ssn = "200001010000"
        memberWithSameSsnAndSignedSomehow.status = MemberStatus.SIGNED

        memberRepository.saveAll(
            listOf(
                memberThatSigns,
                nonSignedMemberWithSameEmailAndSsn,
                nonSignedMemberWithSameEmail,
                nonSignedMemberWithSameSsn,
                otherMemberCompletely,
                memberWithSameSsnAndSignedSomehow
            )
        )

        val members = memberRepository.findNonSignedBySsnOrEmailAndNotId(
            ssn = memberThatSigns.ssn,
            email = memberThatSigns.email,
            memberId = memberThatSigns.id
        )

        assertThat(members).size().isEqualTo(3)
        assertThat(members[0].id).isEqualTo(nonSignedMemberWithSameEmailAndSsn.id)
        assertThat(members[1].id).isEqualTo(nonSignedMemberWithSameEmail.id)
        assertThat(members[2].id).isEqualTo(nonSignedMemberWithSameSsn.id)
    }

    @Test
    fun findAllSignedMembers() {

        val signedMember1 = MemberEntity()
        signedMember1.id = 234L
        signedMember1.email = "signed1@email.com"
        signedMember1.status = MemberStatus.SIGNED

        val signedMember2 = MemberEntity()
        signedMember2.id = 345L
        signedMember2.email = "signed2@email.com"
        signedMember2.status = MemberStatus.SIGNED

        val nonSignedMember = MemberEntity()
        nonSignedMember.id = 456L
        nonSignedMember.email = "nonsigned@email.com"
        nonSignedMember.status = MemberStatus.ONBOARDING

        memberRepository.saveAll(
            listOf(
                signedMember1,
                signedMember2,
                nonSignedMember
            )
        )

        val result = memberRepository.findByStatus(MemberStatus.SIGNED)
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(signedMember1.id)
        assertThat(result[0].status).isEqualTo(MemberStatus.SIGNED)
        assertThat(result[1].id).isEqualTo(signedMember2.id)
        assertThat(result[1].status).isEqualTo(MemberStatus.SIGNED)
    }
}
