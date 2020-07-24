package com.hedvig.memberservice.services

import com.hedvig.external.zignSec.repository.ZignSecSignEntityRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.exceptions.CountryNotFoundException
import com.hedvig.memberservice.services.exceptions.SignedMemberNotFoundException
import com.hedvig.memberservice.util.orNull
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("staging", "development")
class QualityAssuranceServiceImpl(
    private val signedMemberRepository: SignedMemberRepository,
    private val zignSecSignEntityRepository: ZignSecSignEntityRepository
): QualityAssuranceService {
    override fun unsignMember(country: String, ssn: String) =
        when (country.toUpperCase()) {
            "SWEDEN" -> unsignSwedishMember(ssn)
            "NORWAY" -> unsignNorwegianMember(ssn)
            else -> throw CountryNotFoundException(country)
        }

    private fun unsignSwedishMember(ssn: String): Boolean {
        val signedMemberEntity = signedMemberRepository.findBySsn(ssn).orNull()
            ?: throw SignedMemberNotFoundException(
                ssn = ssn,
                repositoryName = "SignedMemberRepository"
            )

        signedMemberRepository.delete(signedMemberEntity)
        return true
    }

    private fun unsignNorwegianMember(ssn: String): Boolean {
        val signedMemberEntity = signedMemberRepository.findBySsn(ssn).orNull()
            ?: throw SignedMemberNotFoundException(
                ssn = ssn,
                repositoryName = "SignedMemberRepository"
            )

        val zignSecSignEntity = zignSecSignEntityRepository.findByPersonalNumber(ssn)
            ?: throw SignedMemberNotFoundException(
                ssn = ssn,
                repositoryName = "ZignSecSignEntityRepository"
            )

        signedMemberRepository.delete(signedMemberEntity)
        zignSecSignEntityRepository.delete(zignSecSignEntity)
        return true
    }
}
