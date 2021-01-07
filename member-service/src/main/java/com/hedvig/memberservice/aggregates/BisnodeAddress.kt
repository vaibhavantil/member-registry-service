package com.hedvig.memberservice.aggregates

import com.hedvig.memberservice.util.logger
import java.util.regex.Pattern

class BisnodeAddress(
    val type: String?,
    val careOf: String?,
    val streetName: String?,
    val streetNumber: String?,
    val entrance: String?,
    val apartment: String?,
    val floor: String?,
    val postOfficeBox: String?,
    val postalCode: String?,
    val city: String?,
    val country: String?,
    val formattedAddress: List<String>?
) {

    companion object {
        fun parseFloorFromApartment(apartmentNo: String?): Int {
            if (apartmentNo == null) {
                return 0
            }
            val compile = Pattern.compile("\\d\\d\\d\\d")
            val matcher = compile.matcher(apartmentNo)
            if (matcher.matches()) {
                try {
                    return apartmentNo.substring(0, 2).toInt() - 10
                } catch (ex: NumberFormatException) {
                    logger.error("Could not parse apartmentnumber: " + ex.message, ex)
                }
            }
            logger.error("ApartmentNo does not match regex. apartmentNo: '{}'", apartmentNo)
            return 0
        }
    }
}
