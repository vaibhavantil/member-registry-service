package com.hedvig.memberservice.services

import com.hedvig.external.zignSec.repository.ZignSecAuthenticationEntityRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("staging", "development")
class QualityAssuranceServiceImpl(
    private val signedMemberRepository: SignedMemberRepository,
    private val zignSecAuthenticationEntityRepository: ZignSecAuthenticationEntityRepository
) : QualityAssuranceService {

    override fun unsignMember(ssn: String): Boolean {
        val signedMemberEntity = signedMemberRepository.findBySsn(ssn)

        val zignSecAuthenticationEntity = zignSecAuthenticationEntityRepository.findByPersonalNumber(ssn)

        if (signedMemberEntity.isPresent) {
            signedMemberRepository.delete(signedMemberEntity.get())
        }
        zignSecAuthenticationEntity?.let {
            zignSecAuthenticationEntityRepository.delete(zignSecAuthenticationEntity)
        }
        return signedMemberEntity.isPresent || zignSecAuthenticationEntity != null
    }
}
