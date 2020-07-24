package com.hedvig.memberservice.services

import com.hedvig.external.zignSec.repository.ZignSecSignEntityRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("staging", "development")
class QualityAssuranceService(
    private val signedMemberRepository: SignedMemberRepository,
    private val zignSecSignEntityRepository: ZignSecSignEntityRepository
) {
    //TODO("Add specific exceptions")
    fun unsignMember(country: String, ssn: String) =
        when (country.toUpperCase()) {
            "SWEDEN" -> unsignSwedishMember(ssn)
            "NORWAY" -> unsignNorwegianMember(ssn)
            else -> throw RuntimeException("No country identified by name $country")
        }

    private fun unsignSwedishMember(ssn: String) {
        val signedMemberEntity = signedMemberRepository.findBySsn(ssn)
            ?: throw RuntimeException("No Swedish member found with SSN $ssn in SignedMemberRepository")

        signedMemberRepository.delete(signedMemberEntity)
    }

    private fun unsignNorwegianMember(ssn: String) {
        val signedMemberEntity = signedMemberRepository.findBySsn(ssn)
            ?: throw RuntimeException("No Norwegian member found with SSN $ssn in SignedMemberRepository")

        val zignSecSignEntity = zignSecSignEntityRepository.findByPersonalNumber(ssn)
            ?: throw RuntimeException("No Norwegian member found with personalNumber $ssn in ZignSecSignEntityRepository")

        signedMemberRepository.delete(signedMemberEntity)
        zignSecSignEntityRepository.delete(zignSecSignEntity)
    }
}
