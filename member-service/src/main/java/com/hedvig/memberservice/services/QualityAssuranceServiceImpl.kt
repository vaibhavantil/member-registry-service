package com.hedvig.memberservice.services

import com.hedvig.external.zignSec.repository.ZignSecSignEntityRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.exceptions.SignedMemberNotFoundException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("staging", "development")
class QualityAssuranceServiceImpl(
    private val signedMemberRepository: SignedMemberRepository,
    private val zignSecSignEntityRepository: ZignSecSignEntityRepository
) : QualityAssuranceService {

    override fun unsignMember(ssn: String): Boolean {
        val signedMemberEntity = signedMemberRepository.findBySsn(ssn)

        val zignSecSignEntity = zignSecSignEntityRepository.findByPersonalNumber(ssn)

        if (signedMemberEntity.isPresent) {
            signedMemberRepository.delete(signedMemberEntity.get())
        }
        zignSecSignEntity?.let {
            zignSecSignEntityRepository.delete(zignSecSignEntity)
        }
        return signedMemberEntity.isPresent && zignSecSignEntity != null
    }
}
