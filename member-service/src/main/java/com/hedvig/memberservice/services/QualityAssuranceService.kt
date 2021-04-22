package com.hedvig.memberservice.services

import com.hedvig.auth.services.UserService
import com.hedvig.external.zignSec.repository.ZignSecAuthenticationEntityRepository
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("staging", "development")
class QualityAssuranceService(
    private val signedMemberRepository: SignedMemberRepository,
    private val zignSecAuthenticationEntityRepository: ZignSecAuthenticationEntityRepository,
    private val userService: UserService,
    private val memberRepository: MemberRepository
) {

    fun unsignMember(ssn: String): Boolean {
        val a = deleteSignedMemberEntity(ssn)
        val b = deleteZignSecAuthenticationEntity(ssn)
        val c = deleteUser(ssn)
        return a || b || c
    }

    private fun deleteSignedMemberEntity(ssn: String): Boolean {
        val signedMemberEntity = signedMemberRepository.findBySsn(ssn)
        if (signedMemberEntity != null) {
            signedMemberRepository.delete(signedMemberEntity.get())
            return true
        }
        return false
    }

    private fun deleteZignSecAuthenticationEntity(ssn: String): Boolean {
        val zignSecAuthenticationEntity = zignSecAuthenticationEntityRepository.findByPersonalNumber(ssn)
        if (zignSecAuthenticationEntity != null) {
            zignSecAuthenticationEntityRepository.delete(zignSecAuthenticationEntity)
            return true
        }
        return false
    }

    private fun deleteUser(ssn: String): Boolean {
        val member = memberRepository.findBySsn(ssn).firstOrNull()
        if (member != null) {
            userService.deleteUserWithAssociatedMemberId(member.id.toString())
            return true
        }
        return false
    }
}
