package com.hedvig.memberservice.query

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SignedMemberRepository : JpaRepository<SignedMemberEntity?, Long?> {
    fun findById(s: Long): SignedMemberEntity?
    fun findBySsn(s: String?): SignedMemberEntity?
}
