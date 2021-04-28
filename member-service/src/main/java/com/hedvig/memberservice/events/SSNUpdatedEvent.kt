package com.hedvig.memberservice.events

import java.lang.RuntimeException
import org.axonframework.serialization.Revision
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.EventUpcaster
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster

@Revision("1.0")
open class SSNUpdatedEvent(
    val memberId: Long,
    val ssn: String,
    val nationality: Nationality
) {
    enum class Nationality {
        SWEDEN, NORWAY, DENMARK
    }

    companion object {
        val upcasters: List<EventUpcaster> = listOf(
            NationalityFromSsnUpcaster("com.hedvig.memberservice.events.SSNUpdatedEvent")
        )
    }

    class NationalityFromSsnUpcaster(
        typeName: String
    ): SingleEventUpcaster() {

        private val inputType = SimpleSerializedType(typeName, null)
        private val outputType = SimpleSerializedType(typeName, "1.0")

        override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
            return intermediateRepresentation.type == inputType
        }

        override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation {
            return intermediateRepresentation.upcastPayload(
                outputType,
                org.dom4j.Document::class.java
            ) { document ->
                val ssn = document.rootElement.element("ssn").text
                document.rootElement.addElement("nationality").text = nationalityFromSsn(ssn)
                document
            }
        }

        private fun nationalityFromSsn(ssn: String): String = when (ssn.length) {
            10 -> "DENMARK"
            11 -> "NORWAY"
            12 -> "SWEDEN"
            else -> throw RuntimeException("Failed upcasting SSN to nationality, length = ${ssn.length}")
        }
    }
}
