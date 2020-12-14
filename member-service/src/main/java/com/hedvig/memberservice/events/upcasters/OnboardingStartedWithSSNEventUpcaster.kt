package com.hedvig.memberservice.events.upcasters

import com.hedvig.memberservice.events.OnboardingStartedWithSSNEvent
import org.axonframework.serialization.SimpleSerializedType
import org.axonframework.serialization.upcasting.event.IntermediateEventRepresentation
import org.axonframework.serialization.upcasting.event.SingleEventUpcaster

class OnboardingStartedWithSSNEventUpcaster: SingleEventUpcaster() {
    override fun canUpcast(intermediateRepresentation: IntermediateEventRepresentation): Boolean {
        return intermediateRepresentation.type == targetType
    }

    override fun doUpcast(intermediateRepresentation: IntermediateEventRepresentation): IntermediateEventRepresentation {
        return intermediateRepresentation.upcastPayload(
            SimpleSerializedType(targetType.name, "1.0"),
            org.dom4j.Document::class.java
        ) { document ->
            val ssn = document.rootElement.element("ssn").text
            document.addElement("nationality").text = ssn.nationalityFromSsn()?.name
            document
        }
    }

    companion object {
        private val targetType = SimpleSerializedType(
            OnboardingStartedWithSSNEvent::class.java.typeName, null
        )
    }
}

