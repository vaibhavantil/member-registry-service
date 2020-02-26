package com.hedvig.external.zignSec.repository.entitys

import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Embeddable
class Error(
    val code: Int,
    val description: String
) {
    constructor(): this( 0, "non-args constructor")
}
