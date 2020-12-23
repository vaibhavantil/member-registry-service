package com.hedvig.memberservice.aggregates

class LivingAddress(
    /** Represents Street, number, entrance  */
    val street: String?,
    val city: String?,
    val zip: String?,
    val apartmentNo: String?,
    val floor: Int?
) {
    fun needsUpdate(
        street: String?, city: String?, zipCode: String?, apartmentNo: String?, floor: Int?): Boolean {
        return if (street == null && city == null && zipCode == null && apartmentNo == null && floor == null) {
            false
        } else this.street != street
            || this.city != city
            || zip != zipCode
            || this.apartmentNo != apartmentNo
            || this.floor != floor
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LivingAddress)
            return false
        if (street != other.street)
            return false
        if (city != other.city)
            return false
        if (zip != other.zip)
            return false
        if (apartmentNo != other.apartmentNo)
            return false
        if (floor != other.floor)
            return false
        return true
    }
}
