package com.hedvig.external.syna.configurations

import com.hedvig.external.syna.SynaClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.apache.http.auth.UsernamePasswordCredentials
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.transport.http.HttpComponentsMessageSender

@Configuration
class SoapConfiguration {

    @Value("\${syna.url}")
    lateinit var synaUrl: String

    @Value("\${syna.user.name}")
    private lateinit var synaUsername: String

    @Value("\${syna.user.password}")
    private lateinit var synaPassword: String

    @Bean
    @Qualifier("syna")
    fun jaxb2Marhsaller(): Jaxb2Marshaller {
        val marshaller = Jaxb2Marshaller()
        marshaller.contextPath = "syna"
        return marshaller
    }

    @Bean
    fun usernamePasswordCredentials(): UsernamePasswordCredentials = UsernamePasswordCredentials(synaUsername, synaPassword)

    @Bean
    @Qualifier("syna")
    fun httpComponentsMessageSender(usernamePasswordCredentials: UsernamePasswordCredentials): HttpComponentsMessageSender {
        val httpComponentsMessageSender = HttpComponentsMessageSender()
        httpComponentsMessageSender.setCredentials(usernamePasswordCredentials)
        httpComponentsMessageSender.setConnectionTimeout(5_000)
        return httpComponentsMessageSender
    }

    @Bean
    fun synaClient(
            @Qualifier("syna") marshaller: Jaxb2Marshaller,
            @Qualifier("syna") messageSender: HttpComponentsMessageSender
    ): SynaClient {
        val client = SynaClient()
        client.webServiceTemplate = WebServiceTemplate()
        client.defaultUri = synaUrl
        client.marshaller = marshaller
        client.unmarshaller = marshaller
        client.messageSenders = arrayOf(messageSender)
        return client
    }
}
