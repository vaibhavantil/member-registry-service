package com.hedvig.memberservice.events

import com.neovisionaries.i18n.CountryCode
import org.axonframework.serialization.Revision
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.EventUpcaster
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster

@Revision("1.0")
data class MemberIdentifiedEvent(
    val memberId: Long,
    val nationalIdentification: NationalIdentification,
    val identificationMethod: String,
    val firstName: String?,
    val lastName: String?
) {
    data class NationalIdentification(
        val identification: String,
        val nationality: Nationality
    )

    enum class Nationality(val countryCode: CountryCode) {
        SWEDEN(CountryCode.SE),
        NORWAY(CountryCode.NO),
        DENMARK(CountryCode.DK);

        companion object {
            fun fromCountryCode(countryCode: CountryCode): Nationality = values().first { it.countryCode == countryCode }
        }
    }


    companion object {
        val upcasters: List<EventUpcaster> = listOf(IdentificationMethodUpcaster())
    }

    private class IdentificationMethodUpcaster: SingleEventUpcaster() {

        private val inputType = SimpleSerializedType(
            "com.hedvig.memberservice.events.MemberIdentifiedEvent", null
        )

        private val outputType = SimpleSerializedType(
            "com.hedvig.memberservice.events.MemberIdentifiedEvent", "1.0"
        )

        override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
            return intermediateRepresentation.type == inputType
        }

        override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation {
            return intermediateRepresentation.upcastPayload(
                outputType,
                org.dom4j.Document::class.java
            ) { document ->
                val translatedValue = when (val oldValue = document.rootElement.element("identificationMethod").text) {
                    "NORWEGIAN_BANK_ID" -> "com.zignsec:BankIDNO"
                    "DANISH_BANK_ID" -> "com.zignsec:NemID"
                    else -> oldValue
                }
                document.rootElement.element("identificationMethod").text = translatedValue
                document
            }
        }
    }

}
