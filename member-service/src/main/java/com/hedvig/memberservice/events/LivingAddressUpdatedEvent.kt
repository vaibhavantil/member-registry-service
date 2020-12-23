package com.hedvig.memberservice.events

import com.hedvig.memberservice.aggregates.BisnodeAddress

class LivingAddressUpdatedEvent(
    val id: Long,
    val street: String?,
    val city: String?,
    val zipCode: String?,
    val apartmentNo: String?,
    val floor: Int?
) : Traceable {

    constructor(memberId: Long, a: BisnodeAddress) : this(
        memberId,
        "${a.streetName} ${a.streetNumber}${a.entrance}",
        a.city,
        a.postalCode,
        a.apartment,
        BisnodeAddress.parseFloorFromApartment(a.apartment)
    )

    override val memberId: Long
        get() = id

    override fun getValues() = mapOf(
        "Street" to street,
        "City" to city,
        "Zip code" to zipCode,
        "Apartment No" to apartmentNo,
        "Floor" to floor
    )
}
